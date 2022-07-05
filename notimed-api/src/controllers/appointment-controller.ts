import express, { Request, Response } from 'express';
import appointmentModel from '../models/appointment.model';
import Appointment from '../models/appointment.model';
import User from '../models/user.model';

const createAppointment = async (req: Request, res: Response) => {
    try {
        
        const { appointmentName, doctorName, appointmentDate, appointmentHour, address, additionalNotes } = req.body;
        if (appointmentName === "" || doctorName === "" || appointmentDate === "" || appointmentHour === "" || address === "")
            throw { status: 400, message: "Fields are empty" }

        //if the appointment is already in the database
        const body = req.body;
        //Datos de usuario
        const {id} = req.params;
        const existingUser = await User.findOne({_id: id});
        const userInfo = {
            id: existingUser?._id,
        }

        const appointment = await Appointment.findOne
            ({
                appointmentName: body.appointmentName,
                doctorName: body.doctorName,
                address: body.address,
                appointmentDate: body.appointmentDate,
                appointmentHour: body.appointmentHour
            });
        if (appointment) {
            throw { status: 409, message: "Appointment already exists" }
        }
        else {
            const newAppointment = new Appointment({
                appointmentName: appointmentName,
                doctorName: doctorName,
                appointmentDate: appointmentDate,
                appointmentHour: appointmentHour,
                address: address,
                additionalNotes: additionalNotes,
                user: userInfo.id
            });
            await newAppointment.save();
            await newAppointment.populate('user', '_id - __v');
        }

    } catch (error) {
        return res
            .status(error.status as number ?? 400)
            .json({ message: error.message ?? JSON.stringify(error) });
    }
}
const deleteAppointment = async(req:Request, res:Response)=>{
    try{
        const {id, id2} = req.params;
        const appointment = await Appointment.findByIdAndDelete({_id: id, user: id2});
        return res.status(200).json({appointment})
    }catch(error){
        return res
        .status(error.status as number ?? 400)
        .json({message: error.message ?? JSON.stringify(error)});
    }
}
const updateAppointment = async(req:Request, res:Response)=>{
    try{
        const {id, id2} = req.params;
        const{appointmentName, doctorName, appointmentDate, appointmentHour, adress,additionalNotes} = req.body;
        const update = await Appointment.findByIdAndUpdate({_id: id, user: id2},{
            appointmentName:appointmentName,
            doctorName:doctorName,
            appointmentDate:appointmentDate,
            appointmentHour:appointmentHour,
            adress:adress,
            additionalNotes:additionalNotes
        })
        res.status(201).send({update})

    }catch(error){
        return res
        .status(error.status as number ?? 400)
        .json({message: error.message ?? JSON.stringify(error)});
    }
}

const getAppointments = async (req: Request, res: Response) => {
    const { limit = 5, skip = 0 } = req.query;
    const {id} = req.params;

    const [total, appointments] = await Promise.all([
        Appointment.countDocuments({user:id}),
        Appointment.find({user:id})
            .skip(Number(skip))
            .limit(Number(limit))
    ])

    res.json({ total, appointments });
};

const getAppointment = async (req: Request, res: Response) => {
    try {
        const { id } = req.params;

        const appointment = await Appointment.findOne({user: id});

        if (!appointment)
            return res.status(404).send({ message: "appointment not found" });

        res.send({ appointment })
    } catch (error) {
        return res
            .status(error.status as number ?? 400)
            .json({ message: error.message ?? JSON.stringify(error) });
    }
};

export {
    getAppointment,
    deleteAppointment,
    updateAppointment,
    getAppointments,
    createAppointment
}