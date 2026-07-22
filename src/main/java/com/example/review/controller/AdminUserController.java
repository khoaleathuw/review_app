package com.example.review.controller;

import com.example.review.dto.UserForm;
import com.example.review.entity.User;
import com.example.review.repository.BranchRepository;
import com.example.review.repository.RoleRepository;
import com.example.review.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;

    public AdminUserController(
            UserService userService,
            RoleRepository roleRepository,
            BranchRepository branchRepository
    ) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
    }

    @GetMapping
    public String list(Model model) {

        model.addAttribute(
                "users",
                userService.getAllUsers()
        );

        return "admin/users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {

        model.addAttribute("userForm", new UserForm());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("branches", branchRepository.findAll());

        return "admin/users/form";
    }

    @PostMapping
    public String create(
            @ModelAttribute UserForm userForm,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.createUser(userForm);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Tạo tài khoản thành công"
            );

            return "redirect:/admin/users";

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/users/new";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggleStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        userService.toggleStatus(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã cập nhật trạng thái tài khoản"
        );

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes
    ) {
        userService.resetPassword(id, newPassword);

        redirectAttributes.addFlashAttribute(
                "success",
                "Đặt lại mật khẩu thành công"
        );

        return "redirect:/admin/users";
    }
}