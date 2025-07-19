package com.example.pathfinder.data.models

data class Postagem(var usuarioPostagemId: String? = null,
                    var descricaoPostagem: String? = null,
                    var fotoPostagem: String? = null, // URL da imagem no Firebase Storage
                    var idPostagem: String? = null,
                    var horaPostagem: String? = null // formato "HH:mm:ss" como string)
)
