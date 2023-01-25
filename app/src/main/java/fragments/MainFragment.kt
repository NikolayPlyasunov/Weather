package fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather.*
import com.example.weather.adapters.VpAdapter
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


const val API_KEY = "7f0968ca690881ed087184007958f87a"
private lateinit var pLauncher: ActivityResultLauncher<String>
private lateinit var binding: FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var fLocationClient: FusedLocationProviderClient
    private val model: MainViewModel by activityViewModels()
    private val fList = listOf(

        DaysFragment.newInstance()
    )
    private val tList = listOf(
       "Прогноз на 5 дней"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            checkPermission()
        init()
        updateCurrentCard()
        requestWeatherData("Chelyabinsk")
    }

    private fun init() = with(binding){
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp){
            tab, pos -> tab.text = tList[pos]
        }.attach()
        ibSync.setOnClickListener {
            requestWeatherData("Chelyabinsk")

            updateCurrentCard()
           // getLocation()
        }
        ibSearch.setOnClickListener{
            DialogManager.searchByNameDialog(requireContext(),object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    if (name != null) {
                        requestWeatherData(name)
                    }
                }
            })
        }

    }

    private fun getLocation(){
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener{
                requestWeatherData("${it.result.latitude}, ${it.result.longitude}")
            }
    }

    private fun updateCurrentCard() = with(binding){
        model.liveDataCurrent.observe(viewLifecycleOwner){
            val maxMinTemp = "${it.maxTemp}/${it.minTemp}°C"
            val sum = it.time.toInt() + it.currentTime.toInt() - 18000
           val time = getDateTime(sum.toString())
           tvData.text = time
            tvCity.text = it.city
            tvCurrentTemp.text = it.currentTemp+"°C"
            tvCondition.text = it.condition
            tvMaxMin.text = "Ощущается как: "+it.feelsLike+"°C"

            Picasso.get().load("https://openweathermap.org/img/wn/"+it.imageUrl+"@2x.png").into(imWeather)
        }
    }
    private fun getDateTime(s: String): String? {
        try {
            val sdf = SimpleDateFormat("dd.MM  HH-mm")
            val netDate = Date(s.toLong() * 1000)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }



    private fun permissionListener(){
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission(){
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(city: String){
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=" +
                "$city&lang=ru&APPID=$API_KEY&units=metric"
        val url2 = "https://api.openweathermap.org/data/2.5/weather?q=" +
               "$city&lang=ru&APPID=$API_KEY&units=metric"
        val queue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            {response->

                parseWeatherData(response)
            },
            {
                Log.d("MyLog", "Volley error: $it")
            }

        )
       val stringRequest2 = StringRequest(
            Request.Method.GET, url2,
            {response->

                parseWeatherData2(response)
            },
            {
                Log.d("MyLog", "Volley error: $it")
            }

        )
        queue.add(stringRequest)
        queue.add(stringRequest2)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
    }

        private fun parseWeatherData2(result: String){
           val mainObject2 = JSONObject(result)

            parseCurrentData(mainObject2)
    }

    private fun parseCurrentData(mainObject: JSONObject){

        val item = WeatherModel(
            mainObject.getString("name"),
            mainObject.getString("dt"),
            mainObject.getJSONArray("weather").getJSONObject(0).getString("description"),
            mainObject.getJSONArray("weather").getJSONObject(0).getString("icon"),

            mainObject.getJSONObject("main").getString("temp").toFloat().toInt().toString(),
            mainObject.getJSONObject("main").getString("temp_max").toFloat().toInt().toString(),
            mainObject.getJSONObject("main").getString("temp_min").toFloat().toInt().toString(),
            "",
            mainObject.getString("timezone"),
            mainObject.getJSONObject("main").getString("feels_like").toFloat().toInt().toString()
        )
        model.liveDataCurrent.value = item

    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel>{
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONArray("list")
        val name = mainObject.getJSONObject("city").getString("name")
        for (i in 0 until daysArray.length()){
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                day.getString("dt"),
                day.getJSONArray("weather").getJSONObject(0).getString("description"),
                day.getJSONArray("weather").getJSONObject(0).getString("icon"),
                day.getJSONObject("main").getString("temp").toFloat().toInt().toString(),
                "",
                "",
                "",
                mainObject.getJSONObject("city").getString("timezone"),
                day.getJSONObject("main").getString("feels_like").toFloat().toInt().toString()

                )
            list.add(item)

        }
        model.liveDataList.value = list
        return list
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}