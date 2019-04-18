package vn.semicolon.zalosupport
data class ZaloUserModel(
    var id: String = "",
    var name: String = "",
    var picture: Picture? = null,
    var birthday: String? = null,
    var gender: String = ""
) {
    data class Picture(
        var data: Data
    ) {
        data class Data(var url: String)
    }

    data class Token(
        var access_token: String,
        var expires_in: String
    )
}
