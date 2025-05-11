package com.example.pathfinder.data.models

import java.util.Date

data class Usuario(var idUsuario: Int? = null,
                   var senhaUsuario: String? = null,
                   var emailUsuario: String? = null,
                   var idadeUsuario: Date? = null, // use Timestamp se estiver usando Firestore
                   var nomeUsuario: String? = null,
                   var enderecoUsuario: Endereco? = null,
                   var localizacaoUsuario: Int? = null,
                   var fotoUsuario: String? = null) // URL ou caminho da foto do usuário

