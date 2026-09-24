package br.ufjf.dcc075.home;

/**
 * Representa uma atividade exibida no menu inicial.
 * Record = classe imutável; o Java gera construtor, getters, equals e toString.
 */
public record Activity(String title, String description, String path) {}