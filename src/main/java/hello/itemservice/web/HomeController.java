package hello.itemservice.web;

import hello.itemservice.domain.member.Member;
import hello.itemservice.web.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class HomeController {

    //@GetMapping("/")
    public String home (
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false)Member loginMember, Model model){
        //세션에 회원 데이터가 없으면 home으로 이동.
        if(loginMember == null){
            return "home";
        }

        //세션에 회원 데이터가 있으면(로그인) loginHome으로 이동.
        model.addAttribute("member", loginMember);
        return "loginHome";


    }

    //사용자지정 애노테이션(@Login)을 생성하여 argumentResolver로 로그인 객체 확인.
    @GetMapping("/")
    public String homeV2 (@Login Member loginMember, Model model){
        //세션에 회원 데이터가 없으면 home으로 이동.
        if(loginMember == null){
            return "home";
        }

        //세션에 회원 데이터가 있으면(로그인) loginHome으로 이동.
        model.addAttribute("member", loginMember);
        return "loginHome";


    }

}
