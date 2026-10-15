package br.ufjf.dcc075.rbac;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {

    /** Todos os papéis com suas permissões (relação PA), para montar a matriz na tela. */
    @EntityGraph(attributePaths = "permissions")
    List<Role> findAllByOrderByIdAsc();
}
