package com.example.walletservice.service.impl;

import com.example.walletservice.client.UserServiceClient;
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
import org.springframework.stereotype.Service;

@Service
@Data
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final ModelMapper modelMapper;
    private final UserServiceClient userServiceClient;

    private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

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
        double totalWalletBalance = wallet.getBalance() + addMoneyRequestDto.getAmount();
        if (totalWalletBalance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }

        wallet.setBalance(totalWalletBalance);
        logger.info("addMoney, newly created wallet with updated details is {}", wallet);

        //Save wallet
        Wallet savedWallet = walletRepository.save(wallet);
        logger.info("addMoney, savedWallet is {}", savedWallet);

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
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for the UserId : "+userId));
        logger.info("getMyWallet, wallet is {}", wallet);

        //Map entity to response dto
        WalletResponseDto walletResponseDto = modelMapper.map(wallet, WalletResponseDto.class);
        logger.info("getMyWallet, walletResponseDto is {}", walletResponseDto);

        return walletResponseDto;
    }

    @Override
    public Double getMyWalletBalance(){
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
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for the userId : "+userId));

        logger.info("getMyWalletBalance, wallet is {}", wallet);

        return wallet.getBalance();
    }

    //Helper method
    private Wallet createNewWallet(Long userId) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(0.0);
        wallet.setCurrency("INR");
        wallet.setStatus(WalletStatus.ACTIVE);
        logger.info("Created new wallet for userId: {}", userId);
        return wallet;
    }
}
