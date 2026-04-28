package com.example.walletservice.service.impl;

import com.example.walletservice.client.UserServiceClient;
import com.example.walletservice.exceptions.InsufficientBalanceException;
import com.example.walletservice.exceptions.ResourceNotFoundException;
import com.example.walletservice.model.dto.AddMoneyRequestDto;
import com.example.walletservice.model.dto.UserResponseDto;
import com.example.walletservice.model.dto.WalletResponseDto;
import com.example.walletservice.model.entity.Wallet;
import com.example.walletservice.model.enums.UserStatus;
import com.example.walletservice.model.enums.WalletStatus;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
@Data
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final ModelMapper modelMapper;
    private final UserServiceClient userServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);
    private static final String WALLET_CACHE_KEY = "wallet:";

    @Override
    @Transactional
    public WalletResponseDto addMoney(AddMoneyRequestDto addMoneyRequestDto){
        logger.info("addMoney, addMoneyRequestDto is {}", addMoneyRequestDto);

        //Fetch current user from User Service via Feign
        UserResponseDto userResponseDto = userServiceClient.getCurrentUser();

        //Check for whether the userId exists or not in user-service
        if(userResponseDto == null || userResponseDto.getUserStatus() != UserStatus.ACTIVE){
            throw new ResourceNotFoundException("User not found or not active");
        }

        long userId = userResponseDto.getUserId();
        logger.info("addMoney, userId is {}", userId);

        //Get or create wallet for the userId
        Wallet wallet = walletRepository.findByUserId(userId)
                            .orElse(createNewWallet(userId));

        //Update wallet balance
        BigDecimal totalWalletBalance = wallet.getBalance().add(addMoneyRequestDto.getAmount());
        if (totalWalletBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }

        wallet.setBalance(totalWalletBalance);
        logger.info("addMoney, newly created wallet with updated details is {}", wallet);

        //Save wallet
        Wallet savedWallet = walletRepository.save(wallet);
        logger.info("addMoney, savedWallet is {}", savedWallet);

        //Redis related: Evict cache after write
        evictWalletCache(savedWallet.getUserId());

        //Map entity to response dto
        WalletResponseDto walletResponseDto = modelMapper.map(savedWallet, WalletResponseDto.class);
        logger.info("addMoney, walletResponseDto is {}", walletResponseDto);

        return walletResponseDto;
    }

    @Override
    public WalletResponseDto getMyWallet(){
        logger.info("getMyWallet");

        //Fetch current user from User Service via Feign
        UserResponseDto userResponseDto = userServiceClient.getCurrentUser();

        //Check for whether the userId exists or not in user-service
        if(userResponseDto == null || userResponseDto.getUserStatus() != UserStatus.ACTIVE){
            throw new ResourceNotFoundException("User not found or not active");
        }

        long userId = userResponseDto.getUserId();
        logger.info("getMyWallet, userId is {}", userId);

        //Find wallet for the userId
        WalletResponseDto walletResponseDto = getWalletWithCache(userId);
        logger.info("getMyWallet, walletResponseDto is {}", walletResponseDto);

        return walletResponseDto;
    }

    @Override
    public BigDecimal getMyWalletBalance(){
        logger.info("getMyWalletBalance");

        //Fetch current user from User Service via Feign
        UserResponseDto userResponseDto = userServiceClient.getCurrentUser();

        //Check for whether the userId exists or not in user-service
        if(userResponseDto == null || userResponseDto.getUserStatus() != UserStatus.ACTIVE){
            throw new ResourceNotFoundException("User not found or not active");
        }

        long userId = userResponseDto.getUserId();
        logger.info("getMyWalletBalance, userId is {}", userId);

        //Find wallet for the userId
        WalletResponseDto walletResponseDto = getWalletWithCache(userId);
        logger.info("getMyWalletBalance, walletResponseDto is {}", walletResponseDto);

        return walletResponseDto.getBalance();
    }

    //Helper method
    private Wallet createNewWallet(Long userId) {
        logger.info("createNewWallet, for userId: {}", userId);

        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("INR");
        wallet.setStatus(WalletStatus.ACTIVE);
        logger.info("Created new wallet for userId: {}", userId);
        return wallet;
    }

    @Override
    public WalletResponseDto getWalletById(Long walletId){
        logger.info("Internal getWalletById, walletId is {}", walletId);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for the walletId : "+walletId));
        logger.info("getWalletById, wallet is {}", wallet);

        //Map entity to response dto
        WalletResponseDto walletResponseDto = modelMapper.map(wallet, WalletResponseDto.class);
        logger.info("getWalletById, walletResponseDto is {}", walletResponseDto);

        return walletResponseDto;
    }

    @Override
    @Transactional
    public WalletResponseDto debit(Long walletId, BigDecimal amount) {
        logger.info("Internal debit, walletId: {}, amount: {}", walletId, amount);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for the walletId : " + walletId));
        logger.info("Internal debit, wallet is {}", wallet);

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet is not active");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for debit operation");
        }

        //Debit amount
        wallet.setBalance(wallet.getBalance().subtract(amount));

        Wallet savedWallet = walletRepository.save(wallet);
        logger.info("Internal debit, savedWallet is {}", savedWallet);
        logger.info("Internal debit, Debit successful. New balance: {}", savedWallet.getBalance());

        //Redis related: Evict cache after write
        evictWalletCache(savedWallet.getUserId());

        WalletResponseDto walletResponseDto = modelMapper.map(savedWallet, WalletResponseDto.class);
        logger.info("Internal debit, walletResponseDto is {}", walletResponseDto);

        return walletResponseDto;
    }

    @Override
    @Transactional
    public WalletResponseDto credit(Long walletId, BigDecimal amount) {
        logger.info("Internal credit, walletId: {}, amount: {}", walletId, amount);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for the walletId : " + walletId));
        logger.info("Internal credit, wallet is {}", wallet);

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet is not active");
        }

        //Credit
        wallet.setBalance(wallet.getBalance().add(amount));

        Wallet savedWallet = walletRepository.save(wallet);
        logger.info("Internal credit, savedWallet is {}", savedWallet);
        logger.info("Internal credit, Credit successful. New balance: {}", savedWallet.getBalance());

        //Redis related: Evict cache after write
        evictWalletCache(savedWallet.getUserId());

        WalletResponseDto walletResponseDto = modelMapper.map(savedWallet, WalletResponseDto.class);
        logger.info("Internal credit, walletResponseDto is {}", walletResponseDto);

        return walletResponseDto;
    }

    @Override
    public WalletResponseDto getWalletByUserId(Long userId){
        logger.info("Internal getWalletByUserId, userId: {}", userId);

        WalletResponseDto walletResponseDto = getWalletWithCache(userId);
        logger.info("Internal getWalletByUserId, walletResponseDto: {}", walletResponseDto);

        return walletResponseDto;

    }

    // ==================== CACHE-AWARE METHODS ====================

    // Main method with Cache-Aside Pattern
    public WalletResponseDto getWalletWithCache(Long userId) {
        logger.info("getWalletWithCache, userId is {}", userId);

        String cacheKey = WALLET_CACHE_KEY + userId;
        logger.info("getWalletWithCache, cacheKey is {}", cacheKey);

        // Step 1: Check Redis Cache
        WalletResponseDto cachedWallet = (WalletResponseDto) redisTemplate.opsForValue().get(cacheKey);
        if (cachedWallet != null) {
            logger.info("getWalletWithCache, Wallet cache hit for userId: {}", userId);
            return cachedWallet;
        }

        logger.info("getWalletWithCache, Wallet cache miss for userId: {}", userId);

        // Step 2: Fetch from Database
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for the userId : "+userId));
        logger.info("getWalletWithCache, wallet is {}", wallet);

        WalletResponseDto walletResponseDto = modelMapper.map(wallet, WalletResponseDto.class);
        logger.info("getWalletWithCache, walletResponseDto is {}", walletResponseDto);

        // Step 3: Cache the result
        redisTemplate.opsForValue().set(cacheKey, walletResponseDto, 10, TimeUnit.MINUTES); // 10 min TTL

        return walletResponseDto;
    }

    // Call this after every write operation (addMoney, debit, credit)
    public void evictWalletCache(Long userId) {
        logger.info("evictWalletCache, userId is {}", userId);

        String cacheKey = WALLET_CACHE_KEY + userId;
        logger.info("evictWalletCache, cacheKey is {}", cacheKey);

        redisTemplate.delete(cacheKey);
        logger.info("evictWalletCache, Wallet cache evicted for userId: {}", userId);
    }

}
