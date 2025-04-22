package com.example.pathfinder.data.models

data class Postagem(var usuarioPostagem: Usuario? = null,
                    var descricaoPostagem: String? = null,
                    var fotoPostagem: String? = null, // URL da imagem no Firebase Storage
                    var idPostagem: Int? = null,
                    var horaPostagem: String? = null // formato "HH:mm:ss" como string)
)
