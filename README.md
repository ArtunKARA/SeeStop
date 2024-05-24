# SeeStop
## Tr
Kullanıcı gitmek istediği lokasyonu telefona sesli olarak söyler. Bu sesli girdi apiler ile metne çevrilir ve ardından kullanıcının gitmek istediği lokasyon harita üzerinde tespit edilir. Konum tespit edildikten sonra, kullanıcının mevcut konumundan gitmek istediği lokasyona doğru süreç başlar ve kullanıcının konumu anbean takip edilir.. Bu süreçte, "10 adım düz git, 5 adım sonra sola dön" gibi sesli yönlendirmeler ile kullanıcının doğru lokasyona ulaşmı sağlanır. Bu yönlendirmeler, belli şablonlardan oluşan kısıtlı bir setten oluşmuş ve tanımlı olabilir, eğer tanımlı değil ise apiden çekilir.

Lokasyona ulaşıldığında sesli yönlendirme sona erer ve uygulama hedefin sona erdiğini bildirir. Eğer çok gürültülü bir ortamdaysam ve sesli yönlendirmeyi uygulama anlamaz ise girdinin tekrar edilmesi için tekrar sesli komut ile kullanıcı uyarılır. Ayrıca yanlış bir yöne yönelirsem, "Rotadan çıkıldı" gibi uyarılarla doğru yola dönmem sağlanabilir.
## En
The user speaks the location he wants to go to the phone. This voice input is translated into text and then the location where the user wants to go is determined on the map. Once the location is determined, movement begins from the current user's location to the location they want to go to. In this process, voice guidance such as "Go straight for 10 meters, turn right, turn left after 5 meters" is used to ensure that the user reaches the correct location. These redirects may consist of a limited set of certain templates, if they are not defined, they will be withdrawn from the API.

When the location is reached, the voice guidance ends and the application notifies you that the destination has ended. If I am in a very noisy environment and the application does not understand the voice guidance, the user is warned with a voice command to repeat the input. Additionally, if I head in the wrong direction, warnings such as "Off route" can help me get back on the right path.

## App Algorithim

![Untitled-2024-02-27-1735](https://github.com/ArtunKARA/SeeStop/assets/76822513/e3d3a57e-282f-4d40-a672-7bbae6ea5d76)
