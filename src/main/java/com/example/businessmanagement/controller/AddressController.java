package com.example.businessmanagement.controller;

import com.example.businessmanagement.entity.Address;
import com.example.businessmanagement.service.AddressService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/address")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }
    // hiển thị danh sách
    @GetMapping("/list")
    public String listAddresses(Model model) {
        List<Address> addresses = addressService.getAllAddresses();
        model.addAttribute("addresses", addresses);
        return "address/list";
    }
    // xóa địa chỉ
    @PostMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (addressService.canDeleteAddress(id)) {
            addressService.deleteAddress(id);
            redirectAttributes.addFlashAttribute("success", "Xóa địa chỉ thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Địa chỉ đang được sử dụng, không thể xóa!");
        }
        return "redirect:/address/list";
    }
}
