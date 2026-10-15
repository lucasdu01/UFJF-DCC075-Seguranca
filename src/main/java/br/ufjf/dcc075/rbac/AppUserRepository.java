package br.ufjf.dcc075.rbac;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Busca o usuário JÁ com papéis e permissões carregados.
     *
     * Sem o @EntityGraph, cada papel dispararia uma query extra para buscar
     * suas permissões (problema N+1). Com ele, o Hibernate traz tudo com JOINs
     * em uma única consulta. Necessário também porque desligamos o
     * open-in-view: fora da transação não há como carregar coleções LAZY.
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<AppUser> findByUsername(String username);

    /** Lista para a tela (relação UA): só precisa dos papéis, não das permissões. */
    @EntityGraph(attributePaths = "roles")
    List<AppUser> findAllByOrderByUsernameAsc();
}
