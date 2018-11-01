# VegaSeal

Android приложение для взаимодействия с электронной пломбой по Bluetooth

### Vega BLE Service

Vega UUID: `9c201400-1c13-8b49-9236-040a580c61b8`

[Описание сервиса](https://github.com/vitalyPru/Android-nRF-Vega/VegaService.md)
  
### Standard BLE Services

DIS UUID: 0000180A-0000-1000-8000-00805F9B34FB (DeviceInformationService)
HTS UUID: 00002A1C-0000-1000-8000-00805F9B34FB (HealthTemperatureService) - пока не работает
BAS UUID: (BatteryService)

### Требования

* Для сборки проекта необходима сторонняя библиотека [Android BLE Library](https://github.com/NordicSemiconductor/Android-BLE-Library/) в той же корневой папке что и основное приложение

* Версия Android 4.3 или выше

### Примечание

* Чтобы сканировать устройства Bluetooth LE, должно быть предоставлено разрешение на определение местоположения, на некоторых телефонах местоположение должно быть включено. При этом приложение никоим образом не использует информацию о местоположении.