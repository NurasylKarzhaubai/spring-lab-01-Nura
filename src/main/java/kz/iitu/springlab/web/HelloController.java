package kz.iitu.springlab.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Value("${app.owner:unknown}")
    private String owner;

    @GetMapping("/hello")
    public Greeting hello(@RequestParam(defaultValue = "world") String name) {
        return new Greeting(
                "Hello, " + name + "!",
                owner,
                LocalDateTime.now()
        );
    }

    @GetMapping("/info")
    public Info info() {
        return new Info(
                owner,
                System.getProperty("java.version"),
                Runtime.getRuntime().availableProcessors()
        );
    }

    @GetMapping("/palindrome")
    public PalindromeResult palindrome(@RequestParam String text) {
        String cleaned = text.replaceAll("\\s+", "").toLowerCase();
        String reversed = new StringBuilder(cleaned).reverse().toString();

        return new PalindromeResult(
                text,
                cleaned.equals(reversed)
        );
    }

    public record Greeting(
            String message,
            String owner,
            LocalDateTime timestamp
    ) { }

    public record Info(
            String owner,
            String javaVersion,
            int cpuCores
    ) { }

    public record PalindromeResult(
            String text,
            boolean palindrome
    ) { }
}