package com.example.walletservice.service.impl;

import com.example.walletservice.exceptions.ResourceNotFoundException;
import com.example.walletservice.model.dto.AddMoneyRequestDto;
import com.example.walletservice.model.dto.WalletResponseDto;
import com.example.walletservice.model.entity.Wallet;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Data
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final ModelMapper modelMapper;

    private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    @Override
    @Transactional
    public WalletResponseDto addMoney(AddMoneyRequestDto addMoneyRequestDto){
        logger.info("addMoney, addMoneyRequestDto is {}", addMoneyRequestDto);

        //Get wallet for the userId
        Wallet wallet = walletRepository.findByUserId(addMoneyRequestDto.getUserId())
                            .orElse(new Wallet());

        //Set wallet userId if it is newly created wallet
        if(wallet.getUserId() == null){
            wallet.setUserId(addMoneyRequestDto.getUserId());
            wallet.setBalance(0.00);
            logger.info("addMoney, newly created wallet with initialized details is {}", wallet);
        }

        //Update wallet balance
        Double totalWalletBalance = wallet.getBalance() + addMoneyRequestDto.getAmount();
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
    public WalletResponseDto getWalletByUserId(Long userId){
        logger.info("getWalletByUserId, userId is {}", userId);

        //Find wallet for the userId
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for the UserId : "+userId));
        logger.info("getWalletByUserId, wallet is {}", userId);

        //Map entity to response dto
        WalletResponseDto walletResponseDto = modelMapper.map(wallet, WalletResponseDto.class);
        logger.info("getWalletByUserId, walletResponseDto is {}", walletResponseDto);

        return walletResponseDto;
    }

    @Override
    public Double getBalanceByUserId(Long userId){
        logger.info("getBalanceByUserId, userId is {}", userId);

        //Find wallet for the userId
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for the userId : "+userId));

        logger.info("getBalanceByUserId, wallet is {}", wallet);

        return wallet.getBalance();
    }
}
