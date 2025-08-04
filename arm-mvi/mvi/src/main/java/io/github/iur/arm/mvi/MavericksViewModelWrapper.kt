package io.github.iur.arm.mvi

import androidx.lifecycle.ViewModel
import io.github.iur.arm.mvi.common.MavericksState

class MavericksViewModelWrapper<VM : MavericksViewModel<S>, S : MavericksState>(
    val viewModel: VM,
) : ViewModel() {
    override fun onCleared() {
        super.onCleared()
        viewModel.onCleared()
    }
}
