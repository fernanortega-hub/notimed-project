import type { NextPage } from 'next'
import Head from 'next/head'
import { MdMailOutline, MdLock } from "react-icons/md";
import InputGroup from '../src/components/Inputs/InputGroup';
import LogInButton from '../src/components/Buttons/LogInButton';
import Top from '../src/components/svg/Top';
import LoginNotimed from '../src/components/svg/LoginNotimed';
import React from 'react';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { signIn } from '../src/services/IdentityService';
import { sessionSave } from '../src/services/SessionStorageService';
import Link from 'next/link';
import { useRouter } from 'next/router';

const Home: NextPage = () => {
    const router = useRouter();

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault()

        const formData = new FormData(e.target as HTMLFormElement);
        const body = Object.fromEntries(formData);

        try {
            const response = await signIn(body)

            if (response.status == 200) {
                router.push("/dashboard")
                sessionSave('token', response.data.token)
            }

        } catch (error) {
            const { response }: any = error

            switch (response.status) {
                case 404:
                    toast("El usuario ingresado no existe", { type: "error" })
                    break;
                case 400:
                    toast("Revisa los datos ingresados", { type: "error" })
                    break;
                case 403:
                    toast("Revisa tus credenciales", { type: "error" })
                    break;
                default:
                    break;
            }
        }

    }

    return (
        <>
            <Head>
                <title> Inicio de sesión </title>
            </Head>
            <ToastContainer />

            <Top title='Inicio de sesión' icon={<LoginNotimed />} />

            <form className='w-full h-full px-4 mt-4 mb-8 space-y-5 md:px-16 items-center flex flex-col justify-center' onSubmit={onSubmit}>
                <InputGroup className="focus:outline-none focus:border-blue-600"
                    placeholder="mrroboto@example.com"
                    maxLength={256}
                    minLength={3}
                    required={true}
                    label="Ingresa tu correo:"
                    type="text"
                    icon={<MdMailOutline className="absolute w-9 h-9 pl-3 text-onSurface-variant" />}
                    identifier="email" />

                <InputGroup className="focus:outline-none focus:border-blue-600"
                    placeholder="Contraseña"
                    minLength={3}
                    required={true}
                    label="Ingresa tu contraseña:"
                    type="password"
                    icon={<MdLock className="absolute w-9 h-9 pl-3 text-onSurface-variant" />}
                    identifier="password" />
                <div className='w-full h-full mt-4 mb-8 space-y-5 md:px-16 items-center flex flex-col justify-center'>
                    <span className='h-auto w-auto mx-2 justify-center items-center text-blue-600 hover:cursor-pointer hover:underline'>
                        ¿Olvidaste tu contraseña?
                    </span>
                    <LogInButton text='Inicia sesión' type='submit' />
                    <span className='h-auto w-auto hover:underline mx-2'>
                        ¿No tienes cuenta?
                    </span>
                    <div className='w-full h-[1px] bg-outline mx-2 max-w-[18.75rem]' />
                    <Link href={'/register'}>
                        <button className="bg-surface border-[1px] border-outline max-w-[18.75rem] labelLarge w-full rounded-full h-10 text-onSurface">
                            <div 
                            className="hover:bg-onSurfaceState-hover focus:bg-onSurfaceState-focus
                            px-7 py-2 w-full rounded-full h-full flex items-center justify-center">
                                Registrate
                            </div>
                        </button>
                    </Link>
                </div>
            </form>
        </>
    )
}

export default Home
