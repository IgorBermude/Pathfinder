package com.example.pathfinder.data.models

import com.google.firebase.Timestamp

data class Usuario(
    var idUsuario: String? = null,
    var senhaUsuario: String? = null,
    var emailUsuario: String? = null,
    var idadeUsuario: Timestamp? = null, // use Timestamp se estiver usando Firestore
    var nomeUsuario: String? = null,
    var enderecoUsuario: Endereco? = null,
    var localizacaoUsuario: Int? = null,
    var fotoUsuario: String? = null) // URL ou caminho da foto do usuário

