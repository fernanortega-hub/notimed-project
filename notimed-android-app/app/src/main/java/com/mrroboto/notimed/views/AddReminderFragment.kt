package com.mrroboto.notimed.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_12H
import com.mrroboto.notimed.NotiMedApplication
import com.mrroboto.notimed.R
import com.mrroboto.notimed.databinding.FragmentAddReminderBinding
import com.mrroboto.notimed.network.ApiResponse
import com.mrroboto.notimed.viewmodels.ReminderViewModel
import com.mrroboto.notimed.viewmodels.ViewModelFactory

class AddReminderFragment : Fragment() {

    private lateinit var binding: FragmentAddReminderBinding

    private val viewModelFactory by lazy {
        val app = requireActivity().application as NotiMedApplication
        ViewModelFactory(app.getReminderRepository())
    }

    private val viewModel: ReminderViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_reminder, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val foodOption = resources.getStringArray(R.array.response_food)
        val arrayAdapterFood = ArrayAdapter(requireContext(), R.layout.dropdown_item, foodOption)

        binding.dropdownOptions.setAdapter(arrayAdapterFood)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        // Handler controlador de los gestos/click al boton de regresar del dispositivo
        requireActivity().onBackPressedDispatcher.addCallback(binding.lifecycleOwner!!) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.warning_title_reminder)
                .setMessage(R.string.warning_body_reminder)
                .setNegativeButton(R.string.no_response) { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(R.string.yes_response) { dialog, _ ->
                    dialog.cancel()
                    findNavController()
                        .navigate(R.id.action_addReminderFragment_to_reminderFragment)
                }
                .show()
        }

        binding.topAppBar.setNavigationOnClickListener {
            it.findNavController().navigate(R.id.action_addReminderFragment_to_reminderFragment)
        }

        binding.cancelButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.warning_title_reminder)
                .setMessage(R.string.warning_body_reminder)
                .setNegativeButton(R.string.no_response) { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(R.string.yes_response) { dialog, _ ->
                    dialog.cancel()
                    it.findNavController()
                        .navigate(R.id.action_addReminderFragment_to_reminderFragment)
                }
                .show()
        }

        binding.dropdownOptions.setOnItemClickListener { _, _, position, _ ->
            if(position == 0) {
                viewModel.currentOption.value = true.toString()
            } else {
                viewModel.currentOption.value = false.toString()
            }
        }


        binding.saveButton.setOnClickListener {
            if (!(isValidRepeat() && isValidDose() && isValidHour() && isValidOption()
                        && isValidMedicine() && isValidRangeDate())
            ) {
                isValidRepeat()
                isValidDose()
                isValidHour()
                isValidOption()
                isValidMedicine()
                isValidRangeDate()
            } else {
                val name = binding.editMedicineName.editText?.text.toString()
                val dose = binding.doseEdit.editText?.text.toString()
                val hour = binding.hourEdit.editText?.text.toString()
                val rangeDate = binding.rangeDate.editText?.text.toString()
                val option = viewModel.currentOption.value.toString().toBoolean()
                val times = binding.editTimesADay.editText?.text.toString().toInt()

                viewModel.createReminder(true, name, rangeDate, dose.toInt(), option, times, hour)
            }
        }

        viewModel.apiResponse.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponse.Loading -> {
                    binding.progressBar3.visibility = View.VISIBLE
                    binding.progressBar3.bringToFront()
                }

                is ApiResponse.Success -> {
                    binding.progressBar3.visibility = View.GONE
                    Toast.makeText(requireContext(), R.string.reminder_created, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_addReminderFragment_to_reminderFragment)
                }
                is ApiResponse.Failure -> {
                    binding.progressBar3.visibility = View.GONE
                    when(it.errorCode) {
                        400 -> Toast.makeText(requireContext(), R.string.general_error, Toast.LENGTH_SHORT).show()
                        else  -> Toast.makeText(requireContext(), R.string.general_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.hourEdit.editText?.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText(R.string.ErrorForHour)
                .build()

            timePicker.show(childFragmentManager, "Tag")

            timePicker.addOnPositiveButtonClickListener {
                timePicker.hour
                binding.hourEdit.editText!!.setText(
                    "%02d:%02d".format(
                        timePicker.hour,
                        timePicker.minute
                    )
                )
            }
        }

        binding.rangeDate.editText?.setOnClickListener {
            // Limitamos la fecha para poder elegir un rango de medicamentos
            val calendarConstraints =
                CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build()

            // Instanciamos el MaterialDatePicker
            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.dateRange_title)
                .setCalendarConstraints(calendarConstraints)
                .build()

            datePicker.show(childFragmentManager, "Tag")

            datePicker.addOnPositiveButtonClickListener {
                binding.rangeDate.editText!!.setText(datePicker.headerText)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val name = binding.editMedicineName.editText
        val dose = binding.doseEdit.editText
        val startDate = binding.rangeDate.editText
        val endDate = binding.rangeDate.editText
        val option = binding.editFoodOption.editText
        val times = binding.editTimesADay.editText
        val hour = binding.hourEdit.editText

        viewModel.currentName.value = name?.text.toString()
        viewModel.currentDose.value = dose?.text.toString()
        viewModel.currentHour.value = hour?.text.toString()
        viewModel.currentStartDay.value = startDate?.text.toString()
        viewModel.currentEndDay.value = endDate?.text.toString()
        viewModel.currentOption.value = option?.text.toString()
        viewModel.currentEveryTimes.value = times?.text.toString()

        viewModel.currentName.observe(viewLifecycleOwner) {
            name!!.setText(it)
        }

        viewModel.currentEveryTimes.observe(viewLifecycleOwner) {
            times!!.setText(it)
        }

        viewModel.currentDose.observe(viewLifecycleOwner) {
            dose!!.setText(it)
        }

        viewModel.currentHour.observe(viewLifecycleOwner) {
            hour!!.setText(it)
        }

        viewModel.currentStartDay.observe(viewLifecycleOwner) {
            startDate!!.setText(it)
        }

        viewModel.currentEndDay.observe(viewLifecycleOwner) {
            endDate!!.setText(it)
        }
    }

    // FUNCIONES DE VALIDACIÓN DE INPUTS
    private fun isValidMedicine(): Boolean {
        val medicine = binding.editMedicineName

        medicine.editText!!.doOnTextChanged { _, _, _, _ ->
            medicine.error = null
        }

        return if (medicine.editText?.text.toString().isEmpty()) {
            medicine.error = getString(R.string.onErrorEmpty)
            false
        } else true
    }

    private fun isValidRepeat(): Boolean {
        val times = binding.editTimesADay

        times.editText!!.doOnTextChanged { _, _, _, _ ->
            times.error = null
        }



        return if (times.editText?.text.toString().isEmpty()) {
            times.error = getString(R.string.ErrorforDropdown)
            false
        } else true
    }

    private fun isValidHour(): Boolean {
        val hour = binding.hourEdit

        hour.editText!!.doOnTextChanged { _, _, _, _ ->
            hour.error = null
        }

        return if (hour.editText?.text.toString().isEmpty()) {
            hour.error = getString(R.string.ErrorForHour)
            false
        } else true
    }

    private fun isValidDose(): Boolean {
        val dose = binding.doseEdit

        dose.editText!!.doOnTextChanged { _, _, _, _ ->
            dose.error = null
        }

        return if (dose.editText?.text.toString().isEmpty()) {
            dose.error = getString(R.string.DoseEmpty)
            false
        } else if (dose.editText!!.text.toString().toFloat() <= 0) {
            dose.error = getString(R.string.DoseLessZero)
            false
        } else true
    }

    private fun isValidRangeDate(): Boolean {
        val date = binding.rangeDate

        date.editText!!.doOnTextChanged { _, _, _, _ ->
            date.error = null
        }

        return if (date.editText?.text.toString().isEmpty()) {
            date.error =
                getString(R.string.ErrorForDate)
            false
        } else {
            true
        }
    }

    private fun isValidOption(): Boolean {
        val option = binding.editFoodOption

        option.editText!!.doOnTextChanged { _, _, _, _ ->
            option.error = null
        }

        return if (option.editText?.text.toString().isEmpty()) {
            option.error = getString(R.string.ErrorforDropdown)
            false
        } else true
    }

}
