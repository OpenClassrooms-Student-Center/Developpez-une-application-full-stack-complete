package com.orion.mdd;

import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Sends what a browser sends with a {@code POST}, {@code PUT} or {@code DELETE}: a first call gives the CSRF cookie,
 * then the request repeats it in a header.
 */
public final class Csrf {

    private Csrf() {
    }

    /**
     * Fetches a CSRF cookie and returns the step that adds it to a request, next to the cookies already there.
     *
     * @param mockMvc
     *            the client of the tested application
     * @return the step to pass to {@code with()}
     * @throws Exception
     *             if the first call fails
     */
    public static RequestPostProcessor from(MockMvc mockMvc) throws Exception {
        Cookie csrfCookie = Objects.requireNonNull(
                mockMvc.perform(get("/api/users/me")).andReturn().getResponse().getCookie("XSRF-TOKEN"));
        return request -> {
            Cookie[] otherCookies = Objects.requireNonNullElse(request.getCookies(), new Cookie[0]);
            request.setCookies(
                    Stream.concat(Arrays.stream(otherCookies), Stream.of(csrfCookie)).toArray(Cookie[]::new));
            request.addHeader("X-XSRF-TOKEN", csrfCookie.getValue());
            return request;
        };
    }

}
