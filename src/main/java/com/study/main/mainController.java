package com.study.main;

import com.study.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class mainController {

    public String home(@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }
        return "index";
    }
}
