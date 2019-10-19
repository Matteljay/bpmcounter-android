package eth.matteljay.bpmcounter

import androidx.lifecycle.ViewModel

class OrientationModel : ViewModel() {
    var countDownAmount = 0
    var consoleOutput = ""
    var currentCount = 0
    var startTime = 0L
}
