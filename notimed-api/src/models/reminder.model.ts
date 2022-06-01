import { model, Schema } from "mongoose";

const Reminder: Schema = new Schema({
    name: {
        type: String,
        required: true
    },
    prescriptions: [{
        dose: Number,
        hour: Date
    }],
    startDay: {
        type: String,
        required: false
    },
    endDay: {
        type: Date,
        required: false
    },
    foodOption: {
        type: Boolean,
        required: false
    },
});

module.exports = model('Reminder', Reminder);