package com.registrodeusuarios.configuracion;

import com.registrodeusuarios.modelos.Rol;
import com.registrodeusuarios.modelos.Usuario;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.registrodeusuarios.repositorios.RolRepositorio;
import com.registrodeusuarios.repositorios.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InicializadorDatos implements CommandLineRunner{
    private final RolRepositorio rolRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception{
        inicializarRoles();
        inicializarAdministrador();
    }

    private void inicializarRoles(){
        if(!rolRepositorio.existsByNombre("ROLE_USER")){
            rolRepositorio.save(new Rol("ROLE_USER"));
        }
        if(!rolRepositorio.existsByNombre("ROLE_ADMIN")){
            rolRepositorio.save(new Rol("ROLE_ADMIN"));
        }
    }

    private void inicializarAdministrador(){
        if(!usuarioRepositorio.existsByNombreUsuario("admin")){
            Collection<Rol> roles = new ArrayList<>();

            Rol rolUsuario = rolRepositorio.findByNombre("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Rol de usuario no encontrado"));

            Rol rolAdmin = rolRepositorio.findByNombre("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol de administrador no encontrado"));

            roles.add(rolUsuario);
            roles.add(rolAdmin);

            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setApellido("del Sistema");
            admin.setNombreUsuario("admin");
            admin.setCorreo("admin@sistema.com");
            admin.setContrasena(passwordEncoder.encode("admin"));
            admin.setEsAdmin(true);
            admin.setRoles(roles);

            usuarioRepositorio.save(admin);
        }
    }
}
