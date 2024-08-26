package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountSuccess() {

        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(15L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("100000012").build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("100000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);


        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDto.getUserId());
        assertEquals("100000013", captor.getValue().getAccountNumber());

    }


    @Test
    void createFirstAccount() {

        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(15L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("100000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);


        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDto.getUserId());
        assertEquals("1000000000", captor.getValue().getAccountNumber());

    }

    @Test
    void createAccount_UserNotFound() {

        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(15L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());


        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());


    }


    @Test
    void createAccount_maxAccountIs10() {

        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(15L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, accountException.getErrorCode());
    }

    @Test
    void deleteAccountSuccess() {

        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(15L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("100000012").build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);


        AccountDto accountDto = accountService.deleteAccount(1L, "1234567890");

        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDto.getUserId());
        assertEquals("100000012", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());


    }


    @Test
    void deleteAccount_UserNotFound() {
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());


        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());

        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }


    @Test
    void deleteAccount_AccountNotFound() {

        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(15L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }


    @Test
    void deleteAccountFailed_userMatch() {

        AccountUser Pobi = AccountUser.builder()
                .name("Pobi").build();
        Pobi.setId(15L);
        AccountUser Harry = AccountUser.builder()
                .name("Harry").build();
Harry.setId(13L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(Pobi));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(Harry)
                        .balance(0L)
                        .accountNumber("100000012").build()));


        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, accountException.getErrorCode());


    }

    @Test
    void deleteAccountFailed_userUnMatch() {

        AccountUser Pobi = AccountUser.builder()
                .name("Pobi").build();
        Pobi.setId(15L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(Pobi));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(Pobi)
                        .balance(100L)
                        .accountNumber("100000012").build()));


        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, accountException.getErrorCode());


    }

    @Test
    void deleteAccountFailed_alreadyUnregistered() {

        AccountUser Pobi = AccountUser.builder()
                .name("Pobi").build();
        Pobi.setId(15L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(Pobi));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(Pobi)
                        .balance(0L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("100000012").build()));


        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());


    }

    @Test
    void successGetAccountByUserId() {
        AccountUser Pobi = AccountUser.builder()
                .name("Pobi").build();
        Pobi.setId(15L);
        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(Pobi)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(Pobi)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(Pobi)
                        .accountNumber("3333333333")
                        .balance(3000L)
                        .build()
        );


        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(Pobi));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);

        List<AccountDto> accountDtos = accountService.getAccountsByUserId(1L);

        assertEquals(3, accountDtos.size());
        assertEquals("1111111111", accountDtos.get(0).getAccountNumber());
        assertEquals(1000, accountDtos.get(0).getBalance());
        assertEquals("2222222222", accountDtos.get(1).getAccountNumber());
        assertEquals(2000, accountDtos.get(1).getBalance());
        assertEquals("3333333333", accountDtos.get(2).getAccountNumber());
        assertEquals(3000, accountDtos.get(2).getBalance());

    }


    @Test
    void failedToGetAccounts() {

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.getAccountsByUserId(1L));

        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());

    }


}