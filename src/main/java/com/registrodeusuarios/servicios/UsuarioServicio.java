package com.registrodeusuarios.servicios;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.registrodeusuarios.modelos.Rol;
import com.registrodeusuarios.modelos.Usuario;
import com.registrodeusuarios.repositorios.RolRepositorio;
import com.registrodeusuarios.repositorios.UsuarioRepositorio;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServicio implements UserDetailsService {
    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

        usuario.getRoles().forEach(rol -> authorities.add(new SimpleGrantedAuthority(rol.getNombre())));
        return new User(usuario.getNombreUsuario(), usuario.getContrasena(), authorities);
    }

    public Usuario guardarUsuario(Usuario usuario) {

        if (usuarioRepositorio.existsByNombreUsuario(usuario.getNombreUsuario())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        if (usuarioRepositorio.existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("El correo electrónico ya está en uso");
        }

        usuario.setContrasena(new BCryptPasswordEncoder().encode(usuario.getContrasena()));

        usuario.setEsAdmin(false);

        Rol rolUsuario = rolRepositorio.findByNombre("ROLE_USER")
                .orElseGet(() -> rolRepositorio.save(new Rol("ROL_USER")));
        usuario.getRoles().add(rolUsuario);

        return usuarioRepositorio.save(usuario);
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepositorio.findAll();
    }

    public Optional<Usuario> obtenerUsuarioPorNombreUsuario(String nombreUsuario) {
        return usuarioRepositorio.findByNombreUsuario(nombreUsuario);
    }

    public Usuario promoverAAdmin(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.isEsAdmin()) {
            throw new RuntimeException("El usuario ya es administrador");
        }
        usuario.setEsAdmin(true);

        Rol rolAdmin = rolRepositorio.findByNombre("ROLE_ADMIN")
                .orElseGet(() -> rolRepositorio.save(new Rol("ROLE_ADMIN")));

        usuario.getRoles().add(rolAdmin);

        return usuarioRepositorio.save(usuario);
    }

    public Usuario degradarUsuario(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException());

        if (!usuario.isEsAdmin()) {
            throw new RuntimeException("El usuario no es administrador");
        }

        usuario.setEsAdmin(false);

        Rol rolAdmin = rolRepositorio.findByNombre("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol de administrador no encontrado"));

        usuario.getRoles().remove(rolAdmin);

        return usuarioRepositorio.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        if (!usuarioRepositorio.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }

        usuarioRepositorio.deleteById(id);
    }

}
