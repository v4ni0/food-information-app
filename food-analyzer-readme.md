# Food Analyzer 🥗🍎
Food Analyzer is a client–server application that provides reliable and structured nutritional information about food products in an easy way.

---

## Table of Contents

1. [Project Structure](#project-structure)
2. [Architecture](#architecture)
3. [Testing](#testing)
4. [Logging](#logging)
5. [How to Use](#how-to-use)

## Project Structure

```
food-information-app/
├── src/
│   └── bg/sofia/uni/fmi/mjt/food/
│       ├── client/
│       │   ├── FoodAnalyzerClient.java
│       │   └── parsing/
│       │       ├── BarcodeParser.java
│       │       └── MessageParser.java
│       │
│       ├── server/
│       │    ├── cache/
│       │        ├── reports/
│       │        ├── barcodes/
│       │        └── keywords/
│       │   ├── FoodAnalyzerServer.java
│       │   ├── ClientRequestHandler.java
│       │   │
│       │   ├── command/
│       │   │   ├── Command.java
│       │   │   ├── CommandParser.java
│       │   │   └── Type.java
│       │   │
│       │   ├── cache/
│       │   │   └── Cache.java
│       │   │
│       │   ├── retriever/
│       │   │   ├── FoodDataRetriever.java
│       │   │   └── model/
│       │   │       ├── FoodDetails.java
│       │   │       ├── FoodReport.java
│       │   │       ├── Nutrient.java
│       │   │       ├── NutrientDetails.java
│       │   │       └── SearchResponse.java
│       │   │
│       │   ├── logging/
│       │   │   └── Logger.java
│       │   │
│       │   └── validation/
│       │       └── Validator.java
│
│
├── test/
```

## Architecture

The system follows a **Client–Server architecture** using TCP/IP sockets and multithreaded server. 

### Port

`5000` (default)

### Client

The `FoodAnalyzerClient`:

- Connects to `localhost:5000`  
- Reads user input from console 
- Uses `MessageParser` to process input, throws `InvalidClientMessageExceptio` if it is not in good format 
- Sends requests to server  
- Waits until receiving `END`  
- Displays formatted server response  

Connection closes when user types:

```
exit
```

### MessageParser
 - Splits get-food-by-barcode commands into parts by whitespace
 - Detects an --img= argument and extracts the image file path if --code= is not present
 - When only --img= is present, uses BarcodeParser to decode the barcode from the image path
 - Throws BarcodeParsingException if the barcode cannot be read from the image
 - Throws InvalidClientMessageException if the message is null, empty, blank, or otherwise invalid

### BarcodeParser

The client supports barcode image decoding using ZXing (Zebra Crossing).

 - Parses absolute path file of an image to barcode identifier. 

If parsing fails:

- `BarcodeParsingException` is thrown    

### Server

The `FoodAnalyzerServer`:

- Listens continuously on port 5000  
- Accepts multiple client connections  
- Uses a thread pool to handle clients concurrently  
- Delegates each connection to `ClientRequestHandler`  

Each `ClientRequestHandler`:

- Reads client commands line-by-line  
- Parses commands using `CommandParser` into `Command` object, which uses builder pattern
- Retrieves data from cache or API, which is decided by `FoodDataRetriever`
- Sends formatted response  
- Appends an `END` marker to signal response completion  
 

### FoodDataRetriever

 - Uses USDA FoodData Central API to get food information.
 - `getFoodReport(id)`
 - Tries to load a food report from cache by FDC id.
 - If missing, calls the API, saves the JSON to cache, filters nutrients, and returns the report.
 - Throws NoResultsFoundException for HTTP 404 and FoodRetrievalException for other errors.
 - `getFoodByKeywords(keywords)`
 - Uses multiple keyword search
 - If not cached, calls the search API, saves the whole result under the keywords, and saves each food with a barcode into the barcode cache.
 - Returns the list of matching foods, or throws NoResultsFoundException/FoodRetrievalException as needed.
 - `getFoodByBarcode(barcode)`
 - Looks up a single product only in the barcode cache by given String barcode.
 - Returns the parsed FoodDetails if found.
 - Throws BarcodeNotFoundException if the barcode is not cached, and FoodRetrievalException on cache I/O errors.
 - Requests are retrieved and parsed into `FoodReport`(which uses) objects for report search or `FoodDetails` objects for search by keywords using Gson

### Cache

 - Saves API responses on memory to speed up future requests
 - Uses a root directory cache with three subfolders: reports, barcodes, keywords
 - Returns null when a requested cache file does not exist
 - Made thread-safe with singleton design pattern via Cache.getInstance(), so that methods could be synchronized by the (only)instance of the class


---
---

## API Integration

The server integrates with the **USDA FoodData Central REST API**:

Base URL:
```
https://api.nal.usda.gov/fdc/v1/
```

All requests require an API key.

### Supported API Calls

• **Search by keywords**
```
GET /foods/search?query=<keywords>&requireAllWords=true&api_key=API_KEY
```
- Example response from the API:
```
{
  "foodSearchCriteria": {
    "query": "raffaello treat",
    "generalSearchInput": "raffaello treat",
    "pageNumber": 1,
    "requireAllWords": true
  },
  "totalHits": 1,
  "currentPage": 1,
  "totalPages": 1,
  "foods": [
    {
      "fdcId": 415269,
      "description": "RAFFAELLO, ALMOND COCONUT TREAT",
      "dataType": "Branded",
      "gtinUpc": "009800146130",
      "publishedDate": "2019-04-01",
      "brandOwner": "Ferrero U.S.A., Incorporated",
      "ingredients": "VEGETABLE OILS (PALM AND SHEANUT). DRY COCONUT, SUGAR, ALMONDS, SKIM MILK POWDER, WHEY POWDER (MILK), WHEAT FLOUR, NATURAL AND ARTIFICIAL FLAVORS, LECITHIN AS EMULSIFIER (SOY), SALT, SODIUM BICARBONATE AS LEAVENING AGENT.",
      "allHighlightFields": "",
      "score": 247.10071
    }
  ]
}
```

• **Get food report by FDC ID**
```
GET /food/<fdcId>?api_key=API_KEY
```

The JSON responses are parsed using Gson into:

- `FoodDetails`
- `FoodReport`
- `SearchResponse`

Before calling the API, the server checks the cache.  
If data is cached → no API request is made.

HTTP errors are translated into user-friendly messages and logged on the server.


### API Key

The server reads the key from:

```
D:/IntelliJ/java/Food Analyzer/FoodAnalzerKey.txt
```

The file must contain only the key.

---

## Testing

The project includes unit testing in /test subfolder and achieves ~76% line coverage using:

- JUnit 6  
- Mockito  

## Logging


```
logs/clientLogs.txt
logs/serverLogs.txt
```

Implemented via:

```
bg.sofia.uni.fmi.mjt.food.server.logging.Logger 
bg.sofia.uni.fmi.mjt.food.client.logging.Logger
```
 - Both implementations use singleton design pattern and synchronization by this instance.

## How to Use?

### 1️⃣ Prepare API Key

Create file:

```
D:/IntelliJ/java/Food Analyzer/FoodAnalzerKey.txt
```
---

### 2️⃣ Start Server

Run main class:

```
FoodAnalyzerServer
```

Port: `5000`

---

### 3️⃣ Start Client

Run:

```
FoodAnalyzerClient
```

---

### 4️⃣ Available Commands

#### Search by keywords
```
get-food beef noodle soup
```

#### Get detailed report
```
get-food-report 415269
```

#### Get by barcode image
```
get-food-by-barcode --img=path/to/image.jpg
get-food-by-barcode --code=validbarcode
```
 
