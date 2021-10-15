class WebSocketConnection {
    constructor() {
        this.websocket = null;
        this.isConnected = false;
        this.heartBeat = null;
        this.onOpenHandler = function () {
            console.info("Connected");
        };
        this.onCloseHandler = function () {
            console.info("Connected");
        };
        this.onMessageHandler = function () {
            console.info("Connected");
        };
        this.onErrorHandler = function (err) {
            console.info(err);
        };
    }

    open(url) {
        try {
            this.websocket = new WebSocket(url);
            this.websocket.onopen = function () {
                this.websocket.send('{"type": "connect", "content": {"name": "web client"}}');
                this.isConnected = true;
                $("#networkStatus").text("Connected! Waiting for data");
                document.getElementById('connect').style.display = 'none';
                document.getElementById('disconnect').style.display = 'inline';
                this.onOpenHandler();
                this.websocket.onclose = function () {
                    this.isConnected = false;
                    document.getElementById('Connection').style.display = 'inline';
                    document.getElementById('primaryTask').style.display = 'none';
                    document.getElementById('overlayCalibration').style.display = "none";
                    document.getElementById('overlayCalibrationAR').style.display = "none";
                    document.getElementById('overlayCalibrationGlance').style.display = "none";
                    document.body.style.backgroundColor = "white";
                    $("#networkStatus").text("Not Connected");
                    document.getElementById('connect').style.display = 'inline';
                    document.getElementById('disconnect').style.display = 'none';
                    this.stopHeartBeat();
                    this.onCloseHandler();
                }.bind(this)
                this.websocket.onmessage = function (event) {
                    this.onMessageHandler(event);
                }.bind(this)
                this.startHeartBeat(45 * 1000)
            }.bind(this)
            this.websocket.onerror = function (ws) {
                this.onErrorHandler(ws.type)
            }.bind(this)
        } catch (ex) {
            this.onErrorHandler(ex)
        }
    }

    startHeartBeat(timeInMillis) {
        if (!this.isConnected || this.heartBeat != null) return
        this.heartBeat = setInterval(
            function () {
                if (!this.isConnected) {
                    this.stopHeartBeat()
                    return
                }
                this.websocket.send("PING")
            }.bind(this), timeInMillis)
    }

    stopHeartBeat() {
        clearInterval(this.heartBeat)
        this.heartBeat = null
    }

    isSupported() {
        return ("WebSocket" in window);
    }

    echo(message) {
        this.websocket.send('{"type": "echo", "content":' + message + '}');
    }


    send(message) {
        this.websocket.send('{"type": "broadcast", "content":' + message + '}');
    }

    sendToLens(message)
    {
        console.log("sendToLens")
        setTimeout(() => this.websocket.send('{"type": "backend", "content":' + message + ', "target": "lens"}'), 100)

    }

    registerOnOpenHandler(handler) {
        this.onOpenHandler = handler.bind(this);
    }

    registerOnMessageHandler(handler) {
        this.onMessageHandler = handler.bind(this);
    }

    registerOnCloseHandler(handler) {
        this.onCloseHandler = handler.bind(this);
    }

    registerOnErrorHandler(handler) {
        this.onErrorHandler = handler.bind(this);
    }

    disconnect() {
        if (this.websocket === null || !this.isConnected) return;
        this.websocket.close();
    }

    loadExperimentData(event) {
        var expConfig = JSON.parse(event.data);
        localStorage["expConfig"] = JSON.stringify(expConfig);
        let metaInfo = JSON.parse(expConfig.metaInfo);
        checkMetaInfo();
        refreshPatient();
        let calibration = metaInfo.CalibrationIncluded;
        let device = metaInfo.Device;
        if (calibration) {
            document.getElementById('primaryTask').style.display = "none";
            switch (device.toString()) {
                case "AR Mode":
                    document.getElementById('overlayCalibrationAR').style.display = "inline";
                    document.body.style.backgroundColor = "black";
                    break;
                case "Glance Mode":
                    document.getElementById('overlayCalibrationGlance').style.display = "inline";
                    document.body.style.backgroundColor = "black";
                    break;
                case "Opaque":
                    document.getElementById('overlayCalibration').style.display = "inline";
                    document.body.style.backgroundColor = "black";
                    break;
                default:
                    break;
            }
        } else {
            document.getElementById('primaryTask').style.display = 'inline';
            document.getElementById('overlayCalibration').style.display = "none";
            document.getElementById('overlayCalibrationAR').style.display = "none";
            document.getElementById('overlayCalibrationGlance').style.display = "none";
            document.body.style.backgroundColor = "white";
        }
        document.getElementById('Connection').style.display = 'none';
    }


    reportInterruptionPoint(interruptionLength) {
        this.websocket.send('{"type": "frontend", "content":' + interruptionLength + '}');

    }

    reportCalibration(){
        this.websocket.send('{"type": "frontend", "dataType": "calibration", "content": "start"}');
    }


    endCalibrationHMD(isEnd) {
        this.websocket.send('{"type": "frontend", "dataType": "calibrationStart", "content": '+ isEnd.toString() +'}');
    }


    sendCSV(csv) {
        console.log("CSV sent");
        this.websocket.send('{"type": "backend", "content":' + csv + '}');
    }

}


const WS = new WebSocketConnection();