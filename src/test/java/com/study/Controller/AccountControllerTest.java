package com.study.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.account.AccountRepository;
import com.study.account.AccountService;
import com.study.account.SignUpForm;
import com.study.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;
    @MockBean
    JavaMailSender javaMailSender;
    @DisplayName("회원 가입 화면 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/sign-up")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));

    }

    @DisplayName("회원 가입 처리- 입력값 오류")
    @Test
    void signUpForm_error() throws Exception {




        mockMvc.perform(post("/sign-up")
                        .param("nickname", "keesun")
                        .param("email", "keesun@email.com")
                        .param("password", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        Account account = accountRepository.findByEmail("keesun@email.com");

        System.out.println("account.toString() ==========  "+account.toString());
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "12345678");
        assertNotNull(account.getEmailCheckToken());
        then(javaMailSender).should().send(any(SimpleMailMessage.class));

    }

    @DisplayName("메일 인증 확인- 입력 오류")
    @Test
    void checkEmail_error() throws Exception {
        mockMvc.perform(get("/check-email-token")
                        .param("token", "asdasdasd")
                        .param("email", "email@naver.com"))
                .andExpect(status().isOk()).andExpect(model()
                        .attributeExists("error")).andExpect(view().name("account/checked-Email"))
                .andExpect(unauthenticated());
    }


    @DisplayName("메일 인증 확인")
    @Test
    void checkEmail() throws Exception {



        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("test@email.com");
        signUpForm.setNickname("test");
        signUpForm.setPassword("12345567");
        accountService.processNewAccount(signUpForm);

        Account byEmail = accountRepository.findByEmail(signUpForm.getEmail());

        mockMvc.perform(get("/check-email-token")
                        .param("token", byEmail.getEmailCheckToken())
                        .param("email", byEmail.getEmail()))
                .andExpect(status().isOk()).andExpect(model()
                .attributeDoesNotExist("error")
                )
                .andExpect(view().name("account/checked-Email"))
                .andExpect(authenticated());
    }

}