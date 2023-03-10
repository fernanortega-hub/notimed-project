
import * as dotenv from 'dotenv'
dotenv.config();//import './env'; // Must be the first import
import logger from 'jet-logger';
import server from './server';
import mongoose from 'mongoose';
import configEnv from './config/config';

if (typeof window !== 'undefined') {
  console.log('You are on the browser')
} else {
  console.log('You are on the server')
}


const PORT = configEnv.port;
// Start server, Iniciando servidor
server.listen(PORT, () => {
    logger.info('Express server started on port: ' + PORT);
});


//Para verificar si mongo DB se conecta correctamente

const db = mongoose.connection;
db.on('error', console.error.bind(console, 'connection error:')); // enlaza el track de error a la consola (proceso actual)
db.once('open', () => {
  console.log('Mongo DB is connected'); // si esta todo ok, imprime esto
});
