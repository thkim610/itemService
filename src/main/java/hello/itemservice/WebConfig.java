package hello.itemservice;

import hello.itemservice.web.argumentresolver.LoginMemberArgumentResolver;
import hello.itemservice.web.filter.LoginCheckFilter;
import hello.itemservice.web.interceptor.LogInterceptor;
import hello.itemservice.web.interceptor.LoginCheckInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    //argumentResolver 사용을 위한 등록
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())
                .order(1) //인터셉터 체인: 첫번째 순서로 적용.
                .addPathPatterns("/**") //모든 경로 인터셉터 허용.
                .excludePathPatterns("/css/**", "/*.ico", "/error"); //해당 경로 인터셉터 제외.

        registry.addInterceptor(new LoginCheckInterceptor())
                .order(2) //인터셉터 체인: 두번째 순서로 적용.
                .addPathPatterns("/**") //모든 경로 인터셉터 허용.
                .excludePathPatterns("/css/**", "/*.ico", "/error",
                        "/", "/members/add", "/login", "/logout"); //해당 경로 인터셉터 제외.
    }

    //@Bean
    public FilterRegistrationBean loginCheckFilter(){
        FilterRegistrationBean<Filter>filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new LoginCheckFilter()); //로그인 인증 여부 필터 적용.
        filterRegistrationBean.setOrder(1); //필터 순서 : 첫번째
        filterRegistrationBean.addUrlPatterns("/*"); //전체 경로 적용.

        return filterRegistrationBean;
    }
}
