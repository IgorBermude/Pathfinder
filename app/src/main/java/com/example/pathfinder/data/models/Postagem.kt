package com.example.pathfinder.data.models

data class Postagem(
    var usuarioPostagemId: String? = null,
    var descricaoPostagem: String? = null,
    var fotoPostagem: String? = null,
    var idPostagem: String? = null,
    var horaPostagem: String? = null,
    var fotoUsuario: String? = null // URL da foto de perfil do usuário
)
