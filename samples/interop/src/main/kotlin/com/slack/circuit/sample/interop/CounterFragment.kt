package com.slack.circuit.sample.interop

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.slack.circuit.sample.counter.CounterScreen
import com.slack.circuit.sample.counter.CounterScreen.Event.Decrement
import com.slack.circuit.sample.counter.CounterScreen.Event.Increment
import com.slack.circuit.sample.interop.databinding.CounterViewBinding
import com.slack.circuit.sample.interop.databinding.FragmentContainerBinding

/**
 * A traditional Android Fragment implementation of a counter UI, mimicking [CounterView].
 * It uses lightweight MVI semantics via [setState] and setters for increment/decrement.
 */
class CounterFragment: Fragment() {

    private lateinit var binding: CounterViewBinding
    private var onIncrementClickListener: OnClickListener? = null
    private var onDecrementClickListener: OnClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = CounterViewBinding.inflate(LayoutInflater.from(context), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.increment.setOnClickListener { onIncrementClickListener?.onClick(it) }
        binding.decrement.setOnClickListener { onDecrementClickListener?.onClick(it) }
        binding.uiImplLabel.text = "(UI: CounterFragment)"
    }

    fun setState(count: Int) {
        binding.count.text = "Count: $count"
        if (count >= 0) {
            binding.count.setTextColor(Color.BLACK)
        } else {
            binding.count.setTextColor(Color.RED)
        }
    }

    fun setOnIncrementClickListener(listener: OnClickListener?) {
        onIncrementClickListener = listener
    }

    fun setOnDecrementClickListener(listener: OnClickListener?) {
        onDecrementClickListener = listener
    }
}

/** An interop compose function that renders [CounterFragment] as a Circuit-powered [AndroidViewBinding]. */
@Composable
fun CounterFragmentComposable(state: CounterScreen.State, modifier: Modifier = Modifier) {
  val eventSink = state.eventSink

    val fragmentManager = (LocalContext.current as? FragmentActivity)?.supportFragmentManager ?: return
    val fragmentTag = stringResource(id = R.string.counter_fragment_tag)
    fun getFragment() = fragmentManager.findFragmentByTag(fragmentTag) as CounterFragment

    AndroidViewBinding(
        factory = { layoutInflater, viewGroup, attachToParent ->
            val binding = FragmentContainerBinding.inflate(layoutInflater, viewGroup, attachToParent)

            getFragment().apply {
                setOnIncrementClickListener { eventSink(Increment) }
                setOnDecrementClickListener { eventSink(Decrement) }
            }

            binding
        },
        modifier = modifier,
        update = {
            getFragment().apply {
                setState(state.count)
            }
        }
    )
}