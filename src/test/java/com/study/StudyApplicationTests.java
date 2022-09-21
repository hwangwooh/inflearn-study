package com.study;

import com.study.account.AccountRepository;
import com.study.account.AccountService;
import com.study.domain.Account;
import com.study.settings.SettingsController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
class StudyApplicationTests {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	AccountService accountService;
	@Autowired
	AccountRepository accountRepository;
	@AfterEach
	void afterEach() {
		accountRepository.deleteAll();
	}
	@WithAccount("hwh")
	@DisplayName("프로필 수정 폼")
	@Test
	void updateProfileForm() throws Exception {
		mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("account"))
				.andExpect(model().attributeExists("profile"));
	}

	@WithAccount("hwh")
	@DisplayName("프로필 수정하기 - 입력값 정상")
	@Test
	void updateProfile() throws Exception {
		String bio = "짧은 소개를 수정하는 경우.";
		mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
						.param("bio", bio)
						.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
				.andExpect(flash().attributeExists("message"));

		Account account = accountRepository.findByNickname("hwh");
		assertEquals(bio, account.getBio());
	}
	@WithAccount("hwh")
	@DisplayName("프로필 수정하기 - 입력값 에러")
	@Test
	void updateProfile_error() throws Exception {
		String bio = "길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 222222222222222222222222222222222222222222222222222222222222222222222222222 ";
		mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
						.param("bio", bio)
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
				.andExpect(model().attributeExists("account"))
				.andExpect(model().attributeExists("profile"))
				.andExpect(model().hasErrors());

		Account account = accountRepository.findByNickname("hwh");
		assertNull(account.getBio());
	}

}
