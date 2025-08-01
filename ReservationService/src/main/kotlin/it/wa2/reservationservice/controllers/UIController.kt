package it.wa2.reservationservice.controllers

import org.springframework.web.bind.annotation.GetMapping

class UIController {
    @GetMapping("/", "")
    fun getUI(): String{
        return "redirect:/ui/"
    }
}

