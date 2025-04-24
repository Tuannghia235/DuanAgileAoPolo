package org.example.registerpolo.ControllerOnl;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ralph-lauren")
public class TrangChuOnlController {
    @GetMapping("/trang-chu")
    public String hienThi(Model model){
        return "BanHangOnl/TrangChuOnl.html"; // <-- Lưu ý phần này
    }
}