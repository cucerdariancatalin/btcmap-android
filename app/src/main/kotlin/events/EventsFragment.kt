package events

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.whenResumed
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.btcmap.databinding.FragmentEventsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class EventsFragment : Fragment() {

    private val model: EventsModel by viewModel()

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    val adapter = EventsAdapter(object : EventsAdapter.Listener {
        override fun onItemClick(item: EventsAdapter.Item) {
            viewLifecycleOwner.lifecycleScope.launch {
                whenResumed {
                    val element = model.selectElementById(item.elementId) ?: return@whenResumed

                    findNavController().navigate(
                        EventsFragmentDirections.actionEventsFragmentToElementFragment(
                            element.id,
                        ),
                    )
                }
            }
        }

        override fun onShowMoreClick() {
            model.onShowMoreItemsClick()
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { toolbar, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            toolbar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = insets.top
            }
            val navBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.list.setPadding(0, 0, 0, navBarsInsets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        WindowCompat.getInsetsController(
            requireActivity().window,
            requireActivity().window.decorView,
        ).isAppearanceLightStatusBars =
            when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> true
                else -> false
            }

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = adapter
        binding.list.setHasFixedSize(true)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                model.state.collect {
                    when (it) {
                        is EventsModel.State.ShowingItems -> {
                            val itemCount = adapter.itemCount

                            adapter.submitList(it.items) {
                                if (itemCount != 0) {
                                    adapter.notifyItemChanged(itemCount - 1)
                                }
                            }
                        }

                        else -> {}
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}