import {check} from 'express-validator';
import express from 'express';
import { login, register, whoami, forgotPassword, resetPassword} from '../controllers/user-controller';
import { existingEmail } from 'src/helpers/db-validators';

const loginRouter = express();

loginRouter.post("/signup", [
    check('password', 'El password tiene que tener 8 o más carácteres').isLength({min:8}),
    check('correo', 'El correo no es válido').isEmail(),
    check('correo').custom(existingEmail),
], register);

loginRouter.post("/signin", login);

loginRouter.get('/whoami', whoami);
loginRouter.post("/forgot-password", forgotPassword);

loginRouter.patch("/reset-password/:id/:token", resetPassword);

module.exports = loginRouter;





