package area

import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import areas.AreaResultModel
import areas.AreasRepo
import element.tags
import elements.ElementsRepo
import icons.iconResId
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonPrimitive
import map.getErrorColor
import map.getOnSurfaceColor
import org.btcmap.R
import org.btcmap.databinding.FragmentAreaBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AreaFragment : Fragment() {

    private val areasRepo: AreasRepo by inject()

    private val elementsRepo: ElementsRepo by inject()

    private val areaResultModel: AreaResultModel by sharedViewModel()

    private var _binding: FragmentAreaBinding? = null
    private val binding get() = _binding!!

    private val adapter: AreaElementsAdapter by lazy {
        val area = runBlocking {
            areasRepo.selectById(
                AreaFragmentArgs.fromBundle(
                    requireArguments()
                ).areaId
            )
        }!!

        val boundingBoxPaddingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            resources.displayMetrics,
        ).toInt()

        AreaElementsAdapter(area, boundingBoxPaddingPx, object : AreaElementsAdapter.Listener {
            override fun onMapClick() {
                areaResultModel.area.update { area }
                findNavController().navigate(R.id.action_areaFragment_to_mapFragment)
            }

            override fun onItemClick(item: AreaElementsAdapter.Item) {
                findNavController().navigate(
                    AreaFragmentDirections.actionAreaFragmentToElementFragment(
                        item.id,
                    ),
                )
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAreaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { appBar, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            appBar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = insets.top
            }
            val navBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.list.setPadding(0, 0, 0, navBarsInsets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val area = runBlocking {
            areasRepo.selectById(
                AreaFragmentArgs.fromBundle(
                    requireArguments()
                ).areaId
            )
        } ?: return

        WindowCompat.getInsetsController(
            requireActivity().window,
            requireActivity().window.decorView,
        ).isAppearanceLightStatusBars =
            when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> true
                else -> false
            }

        binding.toolbar.title = area.name

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val items = elementsRepo.selectByBoundingBox(
                    minLat = area.min_lat,
                    maxLat = area.max_lat,
                    minLon = area.min_lon,
                    maxLon = area.max_lon,
                ).map {
                    val status: String
                    val statusColor: Int

                    if (it.tags()["survey:date"]?.jsonPrimitive?.content.isNullOrBlank() && it.tags()["check_date"]?.jsonPrimitive?.content.isNullOrBlank()) {
                        status = getString(R.string.outdated)
                        statusColor = requireContext().getErrorColor()
                    } else {
                        status = getString(R.string.up_to_date)
                        statusColor = requireContext().getOnSurfaceColor()
                    }

                    AreaElementsAdapter.Item(
                        id = it.id,
                        icon = AppCompatResources.getDrawable(
                            requireContext(), it.iconResId() ?: R.drawable.ic_place,
                        )!!,
                        name = it.tags()["name"]?.jsonPrimitive?.content
                            ?: getString(R.string.unnamed_place),
                        status = status,
                        statusColor = statusColor,
                    )
                }
                adapter.submitList(items.sortedBy { it.name })
                binding.toolbar.subtitle = resources.getQuantityString(
                    R.plurals.d_places,
                    items.size,
                    items.size,
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}