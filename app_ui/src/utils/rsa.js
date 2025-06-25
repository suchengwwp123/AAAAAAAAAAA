import JSEncrypt from 'jsencrypt'

export const rsaEncrypt = {
    encryptor: null,
    publicKey: null,

    init(publicKey) {
        this.encryptor = new JSEncrypt()
        this.publicKey = publicKey
        this.encryptor.setPublicKey(publicKey)
    },

    encrypt(data) {
        if (!this.encryptor || !this.publicKey) {
            throw new Error('请先初始化RSA加密器')
        }
        const encrypted = this.encryptor.encrypt(data)
        if (!encrypted) {
            throw new Error('加密失败，请检查公钥是否正确')
        }
        return encrypted
    }
}