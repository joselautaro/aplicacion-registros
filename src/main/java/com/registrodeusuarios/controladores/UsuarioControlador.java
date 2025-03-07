package com.registrodeusuarios.controladores;

import com.registrodeusuarios.modelos.Usuario;
import com.registrodeusuarios.servicios.UsuarioServicio;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    @GetMapping("/")
    public String inicio(){
        return "redirect:/usuarios";
    }

    @GetMapping("/login")
    public String mostrarLogin(){
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model){
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes){
        try{
            usuarioServicio.guardarUsuario(usuario);
            redirectAttributes.addFlashAttribute("Success", "Usuario registrado correctamente. Ahora puede iniciar sesión");
            return "redirect:/login";
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/registro";
        }
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model){
        model.addAttribute("usuarios", usuarioServicio.obtenerTodosLosUsuarios());
        return "usuarios";
    }

    @GetMapping("/admin")
    public String panelAdmin(Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nombreUsuario = auth.getName();

        Usuario usuarioActual = usuarioServicio.obtenerUsuarioPorNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuarioActual", usuarioActual);

        model.addAttribute("usuarios", usuarioServicio.obtenerTodosLosUsuarios());

        return "admin";
    }


    @PostMapping("/admin/promover")
    public String promoverUsuario(@RequestParam Long id, RedirectAttributes redirectAttributes){
        try{
            usuarioServicio.promoverAAdmin(id);
            redirectAttributes.addFlashAttribute("success", "Usuario promovido a administrador correctamente");
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/degradar")
    public String degradarUsuario(@RequestParam Long id, RedirectAttributes redirectAttributes){
        try {
            usuarioServicio.degradarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Administrador degradado a usuario correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/eliminar")
    public String eliminarUsuario(@RequestParam Long id, RedirectAttributes redirectAttributes){
        try{
            usuarioServicio.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente");
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }


}
