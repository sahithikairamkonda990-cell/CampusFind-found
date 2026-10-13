package com.campusfind.controller;

import com.campusfind.model.*;
import com.campusfind.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final UserRepository users;
    private final ItemRepository items;
    private final ClaimRepository claims;

    public ApiController(UserRepository users, ItemRepository items, ClaimRepository claims) {
        this.users = users; this.items = items; this.claims = claims;
    }

    @Bean
    CommandLineRunner seedAdmin() {
        return args -> {
            if (!users.existsByEmail("admin@campusfind.com")) {
                users.save(new User("CampusFind Admin", "admin@campusfind.com", "admin123", "ADMIN"));
            }
            if (items.count() == 0) {
                Item i = new Item();
                i.setItemName("Blue water bottle");
                i.setDescription("Blue reusable bottle found near the library.");
                i.setCategory("Accessories");
                i.setLocation("Central Library");
                i.setItemDate(LocalDate.now());
                i.setType("FOUND");
                i.setStatus("FOUND");
                i.setReporterEmail("admin@campusfind.com");
                items.save(i);
            }
        };
    }

    private Map<String, Object> publicUser(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId()); m.put("name", u.getName());
        m.put("email", u.getEmail()); m.put("role", u.getRole());
        return m;
    }

    @PostMapping("/auth/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "");
        if (name.isBlank() || email.isBlank() || password.length() < 4)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter name, email and a password of at least 4 characters.");
        if (users.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        User u = users.save(new User(name, email, password, "USER"));
        return publicUser(u);
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "");
        User u = users.findByEmail(email).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        if (!u.getPassword().equals(password))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        return publicUser(u);
    }

    @GetMapping("/items")
    public List<Item> getItems(@RequestParam(required = false) String q,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String category) {
        return items.search(blankToNull(q), blankToNull(type), blankToNull(category));
    }

    @PostMapping("/items")
    public Item createItem(@RequestBody Item item) {
        if (item.getItemName() == null || item.getItemName().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item name is required.");
        if (item.getType() == null || !(item.getType().equals("LOST") || item.getType().equals("FOUND")))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type must be LOST or FOUND.");
        if (item.getItemDate() == null) item.setItemDate(LocalDate.now());
        item.setStatus(item.getType());
        return items.save(item);
    }

    @PostMapping("/claims")
    public Claim createClaim(@RequestBody Claim claim) {
        if (claim.getItemId() == null || !items.existsById(claim.getItemId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found.");
        if (claim.getClaimantEmail() == null || claim.getClaimantEmail().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please log in before claiming.");
        claim.setStatus("PENDING");
        return claims.save(claim);
    }

    @GetMapping("/claims")
    public List<Claim> getClaims(@RequestParam(required = false) String email) {
        List<Claim> all = claims.findAllByOrderByCreatedAtDesc();
        if (email == null || email.isBlank()) return all;
        return all.stream().filter(c -> email.equalsIgnoreCase(c.getClaimantEmail())).toList();
    }

    @PutMapping("/claims/{id}")
    public Claim updateClaim(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Claim c = claims.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found."));
        String status = body.getOrDefault("status", "");
        if (!List.of("APPROVED", "REJECTED").contains(status))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be APPROVED or REJECTED.");
        c.setStatus(status);
        Claim saved = claims.save(c);
        if (status.equals("APPROVED")) {
            items.findById(c.getItemId()).ifPresent(item -> {
                item.setStatus("CLAIMED");
                items.save(item);
            });
        }
        return saved;
    }

    @PutMapping("/items/{id}/status")
    public Item updateItemStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Item item = items.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found."));
        String status = body.getOrDefault("status", "");
        if (!List.of("LOST", "FOUND", "CLAIMED", "RETURNED").contains(status))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid item status.");
        item.setStatus(status);
        return items.save(item);
    }

    private String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
