# :computer: leego-assignment-2021

- 개발언어: Kotlin  
- 프레임워크: Spring Boot, Spring Cloud

## 개요
- **api-server**: OpenAPI를 통해 지역 정보를 검색하고 관련된 이미지 링크를 함께 제공  
- gateway: 마이크로서비스 활용시 라우팅을 처리  
- service-discovery: 마이크로서비스 활용시 각 서비스의 연결 상태 관리  


## 실행 방법
### case 1) Standalone
gateway 및 service-discovery 없이 api-server 단독으로 테스트가 가능합니다.  
api-server를 다운 받은 후 Maven을 통해 빌드합니다.  
동시 실행을 위해 Random port로 설정되어있으므로 반드시 port를 지정해야 합니다.

```bash
$ mvn package
$ cd target
$ java -jar -Dserver.port=8080 api-server-0.0.1-SNAPSHOT.jar
```

### case 2) Microservice
다수의 api-server를 동시에 사용할 수 있습니다.  
api-server, gateway, service-discovery를 각각 다운 받은 후 Maven을 통해 빌드합니다.  
실행 순서는 크게 중요하지 않으나 service-discovery -> gateway -> api-server 순서로 실행하는 것을 권장합니다.  
> Request는 반드시 gateway로 전송해야 합니다.  

> :warning:주의) Eureka 설계 특성상 [서비스의 UP/DOWN 감지에 약간의 시간이 소요](https://projects.spring.io/spring-cloud/spring-cloud.html#_why_is_it_so_slow_to_register_a_service)될 수 있습니다.  
> 이로 인해 **일시적으로 통신이 원활하지 않는 현상이 발생**할 수도 있습니다.  
> 이러한 현상이 발생하신 경우 일정 시간 대기하거나 gateway로 request를 수회 반복하시면 해결됩니다.

```bash
**service-discovery**
$ mvn package
$ cd target
$ java -jar service-discovery-0.0.1-SNAPSHOT.jar


**gateway**
$ mvn package
$ cd target
$ java -jar gateway-0.0.1-SNAPSHOT.jar


**api-server**
$ mvn package
$ cd target
$ java -jar api-server-0.0.1-SNAPSHOT.jar &
$ java -jar api-server-0.0.1-SNAPSHOT.jar &
$ java -jar api-server-0.0.1-SNAPSHOT.jar &
.....(필요한만큼 동시 실행 가능)

**service-discovery와 동일 서버가 아닐 경우 아래와 같이 실행**
$ java -jar -Deureka.client.service-url.default-zone=http://{service-discovery-ip:port}/eureka/ gateway-0.0.1-SNAPSHOT.jar &
$ java -jar -Deureka.client.service-url.default-zone=http://{service-discovery-ip:port}/eureka/ api-server-0.0.1-SNAPSHOT.jar &
```

| Service name  | Default port |
| ------------- | ------------- |
| gateway  | 8080  |
| service-discovery  | 8761  |
| api-server  | 0 (Random port)  |


## API 명세
### 1. 지역 정보 검색
카카오 지역 API를 사용하여 검색 결과를 받아오며, 장소의 상호명을 이용해 카카오 이미지 검색의 결과를 함께 제공합니다.  
카카오 지역 API를 정상적으로 이용할 수 없거나 조회되는 내용이 없을 경우 네이버 지역 API를 사용합니다.  
> 네이버 지역 API는 Pagination이 지원되지 않으며, size 최대값은 5, page 최대값은 1로 제약되어 있습니다.  

## Request
```http
GET /search?query=카카오&size=1&page=1
```

```bash
$ curl http://{ip:port}/search?query=kakao&size=1&page=1
$ curl -G "http://{ip:port}/search" --data-urlencode "query=카카오" -d "size=1" -d "page=1"
```

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `query` | `string` | **(필수값)** 검색할 내용 입력 |
| `size` | `int` | 한 페이지에서 보일 개수 (1 ~ 15) |
| `page` | `int` | 조회할 페이지 번호 (1 ~ 45) |

## Response
### 카카오 지역 API 동작시 Sample
```javascript
{
    "meta": {
        "totalPages": 466,
        "totalElements": 466,
        "currentPage": 1,
        "pageSize": 1
    },
    "places": [
        {
            "address_name": "경기 성남시 분당구 삼평동 681",
            "category_group_code": "",
            "category_group_name": "",
            "category_name": "서비스,산업 > 인터넷,IT > 포털서비스",
            "id": "18577297",
            "phone": "1899-1326",
            "place_name": "카카오 판교오피스",
            "place_url": "http://place.map.kakao.com/18577297",
            "road_address_name": "경기 성남시 분당구 판교역로 235",
            "x": "127.10820996551278",
            "y": "37.402054243658846",
            "distance": "",
            "imageUrls": [
                "https://blogfiles.pstatic.net/MjAxODEwMjRfMTYw/MDAxNTQwMzg5NzM3MDky.5qPzikAjvvp-i4icKQ6mv4eTKrvzeXo45sU0rSwxhP8g.nSYwJnSUOikCoG7lmw3RMUzym26fggNi_mZ1Oss28msg.JPEG.junu23/KakaoTalk_20181024_225956642.jpg?type=w1",
                "http://postfiles16.naver.net/MjAxODEwMjVfMzcg/MDAxNTQwNDc3NzkxNTE0.5hLhrvOOR__jhgqkStBUfeW2X4mTu4wncqHse9r3wJ8g.7cpO5wpvGLgKo5gwMbp1_qtTISFEQrwkaKoDM5PskrQg.JPEG.yoo9799/20181018_135143.jpg?type=w966",
                "http://postfiles9.naver.net/20161006_296/soohoh88_1475732534356D9k7z_JPEG/KakaoTalk_20161004_221724230.jpg?type=w2"
            ]
        }
    ]
}
```

### 네이버 지역 API 동작시 Sample
```javascript
{
    "meta": {
        "pageSize": 1,
        "message": "네이버 지역 API는 Pagination을 지원하지 않습니다."
    },
    "places": [
        {
            "title": "카카오 한남오피스",
            "link": "http://www.daumkakao.com/",
            "category": "서비스,산업>IT서비스",
            "description": "",
            "telephone": "",
            "address": "서울특별시 용산구 한남동 714 일신빌딩 5층",
            "roadAddress": "서울특별시 용산구 한남대로 98 일신빌딩 5층",
            "mapx": "312292",
            "mapy": "548828"
            "imageUrls": [
                "http://imgnews.naver.net/image/5364/2016/12/15/0000123237_001_20161215142042599.jpg",
                "http://imgnews.naver.net/image/5497/2017/08/17/44887_84521_2411_20170817102633927.jpg",
                "http://imgnews.naver.net/image/293/2014/10/01/daumkakao_201410011-800x450_99_20141001124204.jpg"
            ]
        }
    ]
}
```

### 2-1. 인키 키워드 목록
사용자들이 많이 검색한 순서대로 최대 10개의 검색 키워드를 제공합니다.  
대소문자를 구분하며 검색 결과가 없거나 검색 결과를 제대로 가져오지 못하더라도 누적됩니다.  
각 마이크로서비스에서 집계한 결과를 제공하며 마이크로서비스가 1개라도 중단될 경우 결과가 변동될 수 있습니다.  

## Request
```http
GET /ranking
```

```bash
$ curl http://{ip:port}/ranking
```

### 2-2. 인키 키워드 목록 (동기화 방식)
ranking API와 기능은 동일하지만 처리 방식이 다릅니다.  
주기적으로 마이크로서비스들과 통신하면서 최신 데이터를 유지합니다.  
동기화 구간 사이에 요청될 경우 실제 데이터와 차이가 발생할 수 있습니다.  
> 매 3초마다 동기화 진행

## Request
```http
GET /syncRanking
```

```bash
$ curl http://{ip:port}/syncRanking
```

## Response
```javascript
[
    {
        "rank": 1,
        "queryName": "카카오",
        "count": 28
    },
    {
        "rank": 2,
        "queryName": "스타벅스",
        "count": 14
    },
    {
        "rank": 3,
        "queryName": "카페",
        "count": 13
    },
    {
        "rank": 4,
        "queryName": "주유소",
        "count": 8
    },
    {
        "rank": 5,
        "queryName": "sk",
        "count": 7
    },
    {
        "rank": 6,
        "queryName": "github",
        "count": 7
    },
    {
        "rank": 7,
        "queryName": "오드이븐",
        "count": 3
    },
    {
        "rank": 8,
        "queryName": "테스트",
        "count": 2
    },
    {
        "rank": 9,
        "queryName": "대만식햄치즈샌드",
        "count": 1
    },
    {
        "rank": 10,
        "queryName": "매료",
        "count": 1
    }
]
```

## POM Dependencies list
- 필수적인 Spring boot & Language dependency 제외 (h2, jpa 포함)  

| Dependency  | Description |
| ------------- | ------------- |
| spring-cloud-starter-netflix-zuul  | 마이크로서비스 라우팅 및 로드밸런싱 처리  |
| spring-cloud-starter-netflix-eureka-server  | 서비스 디스커버리 서버용 (상태 관리)  |
| spring-cloud-starter-netflix-eureka-client  | 서비스 디스커버리 클라이언트용 (정보 및 Heart beat 전송)  |
| spring-cloud-starter-netflix-hystrix  | 서킷 브레이커 패턴 구현 용도  |
| unirest-java  | 경량화 HTTP 클라이언트  |
| gson  | JSON serialization/deserialization 용도  |
