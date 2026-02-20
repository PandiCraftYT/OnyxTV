package com.example.onyxapp

data class Channel(
    val name: String,
    val url: String,
    val logo: String? = null,
    val group: String? = null
)

object ChannelsConfig {
    // Aquí puedes pegar tu lista M3U tal cual la tienes
    val M3U_SOURCE = """
#EXTM3U
#EXTINF:-1 tvg-logo="https://static.wikia.nocookie.net/logopedia/images/c/c0/Las_Estrellas_2017_Navidad.svg/revision/latest/scale-to-width-down/250?cb=20230202024114&path-prefix=es" group-title="MEXICO",LAS ESTRELLAS HD
http://172.93.102.112:8989/158630_.m3u8
#EXTINF:-1 tvg-logo="https://static.wikia.nocookie.net/logopedia/images/c/c0/Las_Estrellas_2017_Navidad.svg/revision/latest/scale-to-width-down/250?cb=20230202024114&path-prefix=es" group-title="MEXICO",LAS ESTRELLAS HD OP2
http://190.60.37.154:45000/play/a03m/index.m3u8
#EXTINF:-1 tvg-logo="https://static.wikia.nocookie.net/logopedia/images/c/c0/Las_Estrellas_2017_Navidad.svg/revision/latest/scale-to-width-down/250?cb=20230202024114&path-prefix=es" group-title="MEXICO",LAS ESTRELLAS HD OP3
https://dai.google.com/ssai/event/_e1s_U52SCGL6zTnlTrbVQ/master.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/2/22/Estrella_TV_Logo_2020.png/509px-Estrella_TV_Logo_2020.png?20210701011117" group-title="MEXICO",ESTRELLA TV
https://estrellatv-roku.amagi.tv/playlist.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/2/22/Estrella_TV_Logo_2020.png/509px-Estrella_TV_Logo_2020.png?20210701011117" group-title="MEXICO",ESTRELLA TV OP2
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5cf0622da00ca1e2f6fac712/master.m3u8?advertisingId=&appName=web&appVersion=5.16.0-d477896b413cece569cca008ddae951d02cadc9e&app_name=web&clientDeviceType=0&clientID=65dca7cb-5c3a-4186-9083-f702118added&clientModelNumber=na&deviceDNT=true&deviceId=65dca7cb-5c3a-4186-9083-f702118added&deviceLat=42.0048&deviceLon=-87.9954&deviceMake=Chrome%2BMobile&deviceModel=web&deviceType=web&deviceVersion=83.0.4103.106&marketingRegion=US&serverSideAds=true&sessionID=b8df70d0-afa5-11eb-b521-0242ac110002&sid=b8df70d0-afa5-11eb-b521-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://estrellatv.com/img/shows/noticias-estrellatv/logo.png" group-title="MEXICO",ESTRELLA TV NOTICIAS
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/60492e6d7f3f560007ab0f62/master.m3u8?advertisingId=&appName=web&appVersion=5.16.0-d477896b413cece569cca008ddae951d02cadc9e&app_name=web&clientDeviceType=0&clientID=65dca7cb-5c3a-4186-9083-f702118added&clientModelNumber=na&deviceDNT=true&deviceId=65dca7cb-5c3a-4186-9083-f702118added&deviceLat=42.0048&deviceLon=-87.9954&deviceMake=Chrome%2BMobile&deviceModel=web&deviceType=web&deviceVersion=83.0.4103.106&marketingRegion=US&serverSideAds=true&sessionID=b8df70d0-afa5-11eb-b521-0242ac110002&sid=b8df70d0-afa5-11eb-b521-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/azteca_uno-mediano.png" group-title="MEXICO",AZTECA UNO
https://mdstrm.com/live-stream-playlist/609b243156cca108312822a6.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/azteca_uno-mediano.png" group-title="MEXICO",AZTECA UNO OP2
https://stream.ads.ottera.tv/playlist.m3u8?network_id=5396
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/b/b6/Azteca_7_variante_blanca_2021.png" group-title="MEXICO",AZTECA 7 HD
http://tv.diablotv.net:8080/GeraJuvera25/m3uBHfmEeh/87572
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/b/b6/Azteca_7_variante_blanca_2021.png" group-title="MEXICO",AZTECA 7  OP2
https://mdstrm.com/live-stream-playlist/609ad46a7a441137107d7a81.m3u8
#EXTINF:-1 tvg-logo="https://www.foromedios.com/uploads/gallery/category_2/gallery_1163_2_713.png" group-title="MEXICO",AZ CLIC HD
https://stream.ads.ottera.tv/playlist.m3u8?network_id=4827
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/tv_azteca_mundo-mediano.png" group-title="MEXICO",AZ MUNDO
http://45.4.85.214:58001/play/a02f
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/tv_azteca_cinema-mediano.png" group-title="MEXICO",AZ CINEMA 
http://104.238.205.28:8989/278464_.m3u8
#EXTINF:-1 tvg-logo="https://i.imgur.com/jJ6v5ea.png" group-title="MEXICO",CANAL 5 HD
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/162980.m3u8
#EXTINF:-1 tvg-logo="https://i.imgur.com/jJ6v5ea.png" group-title="MEXICO",CANAL 5 HD OP 2 
http://tv.diablotv.net:8080/GeraJuvera25/m3uBHfmEeh/87598
#EXTINF:-1 tvg-logo="https://cdn140.picsart.com/278369393029211.png" group-title="MEXICO",NU9VE
https://sneoplus.vip:8443/play/4csat81f7AeFdCdwen9KanXdMzp_KSTITXlXI_wLo3c/ts
#EXTINF:-1 tvg-logo="https://cdn140.picsart.com/278369393029211.png" group-title="MEXICO",NU9VE HD 
http://xcm.budtv-ultra.com:25461/budtvline/8Y3ymUpJESps/278360
#EXTINF:-1 tvg-logo="https://cdn140.picsart.com/278369393029211.png" group-title="MEXICO",NU9VE HD OP 2
http://offshore.lat:8080/changeESUS@/7QcXgwrCWfYv/230603.ts
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/e/e0/Galavisi%C3%B3n_logo2012.png" group-title="MEXICO",GALAVISION
http://tv14s.xyz:8080/FxQ7fU/508292/58770
#EXTINF:-1 tvg-logo="https://aspire.jo/wp-content/uploads/2019/12/Univision-white_a4ccc61316bb33480d5c80d80af4b7f2.png" group-title="MEXICO",UNIVISION MIAMI
http://hmbqa6.sekkd.com:80/live/amy5_y3zf4s/gnutz2k9/671603.m3u8
#EXTINF:-1 tvg-logo="https://aspire.jo/wp-content/uploads/2019/12/Univision-white_a4ccc61316bb33480d5c80d80af4b7f2.png" group-title="MEXICO",UNIVISION LOS ANGELES
http://tv14s.xyz:8080/FxQ7fU/508292/108248
#EXTINF:-1 tvg-logo="https://aspire.jo/wp-content/uploads/2019/12/Univision-white_a4ccc61316bb33480d5c80d80af4b7f2.png" group-title="MEXICO",UNIVISION
http://190.121.42.18:8000/play/a01x/79124588.m3u8
#EXTINF:-1 tvg-logo="https://st1.uvnimg.com/f0/af/98382f8a420a8b369bb4e7e1ff11/umas-1-spot-wht.png" group-title="MEXICO",UNIMAS
http://tv14s.xyz:8080/FxQ7fU/508292/57969
#EXTINF:-1 tvg-logo="https://st1.uvnimg.com/f0/af/98382f8a420a8b369bb4e7e1ff11/umas-1-spot-wht.png" group-title="MEXICO",UNIMAS LA
http://nocable.cc:8080/live/PyRR3Hy/DthK0fv/57969.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/a_mas-mediano.png" group-title="MEXICO",A PLUS
http://00322.xyz:8000/live/saabj84/CNGUPDY6RA/3668.ts
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/once_tv_mexico-mediano.png" group-title="MEXICO",ONCE HD
https://d24sa4vr9gvjv.cloudfront.net/index_720.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/teleformula-mediano.png" group-title="MEXICO",TELEFORMULA
https://stream.ads.ottera.tv/playlist.m3u8?network_id=4505
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/mexiquense_tv-mediano.png" group-title="MEXICO",MEXIQUENSE 3.1 
https://5e50264bd6766.streamlock.net/mexiquense/videomexiquense/playlist.m3u8 
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/Logo-Imagen-TV-MX.png/800px-Logo-Imagen-TV-MX.png" group-title="MEXICO",IMAGEN TV
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278464.m3u8
#EXTINF:-1 tvg-logo="https://static.wikia.nocookie.net/logopedia/images/4/4b/Multimedios_Estrellas_de_Oro_%281991_Symbol%29.svg/revision/latest/scale-to-width-down/250?cb=20210721224718" group-title="MEXICO",MULTIMEDIOS MONTERREY
https://mdstrm.com/live-stream-playlist/57b4dbf5dbbfc8f16bb63ce1.m3u8
#EXTINF:-1 tvg-logo="https://static.wikia.nocookie.net/logopedia/images/4/4b/Multimedios_Estrellas_de_Oro_%281991_Symbol%29.svg/revision/latest/scale-to-width-down/250?cb=20210721224718" group-title="MEXICO",MULTIMEDIOS CDMX 
https://mdstrm.com/live-stream-playlist/5f2d9d6ff17144074bd8a284.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/aprende_tv-mediano.png" group-title="MEXICO",APRENDE TV
http://ott.cdn.iutpcdn.com/LIVE/H01/CANAL401/PROFILE03.m3u8
#EXTINF:-1 tvg-logo="https://directostv.teleame.com/wp-content/uploads/2017/10/tvmas.png" group-title="MEXICO",TVMAS
http://ott.cdn.iutpcdn.com/LIVE/H01/CANAL2324/PROFILE03.m3u8
#EXTINF:-1 tvg-logo="https://onceninasyninos.tv/wp-content/uploads/2019/07/cropped-Nuevo_Logo_ON-2019-e1561997594507.png" group-title="MEXICO",ONCE NIÑOS
http://ott.cdn.iutpcdn.com/LIVE/H01/CANAL3315/PROFILE03.m3u8
#EXTINF:-1 tvg-logo="https://cdn.mitvstatic.com/channels/co_tvc_m.png" group-title="MEXICO",TVC  
http://tv.proyectox.vip:8080/L0vjEGB3MC/GF8Nqw8gZ5/26735
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Russia-today-logo.svg/125px-Russia-today-logo.svg.png" group-title="MEXICO",RT
http://magi-stv.net:8080/rhinoxCP/TG66W5jsaLRn/25753
#EXTINF:-1 tvg-logo="https://vignette.wikia.nocookie.net/logopedia/images/7/74/Foro_TV_2016_logo.png/revision/latest?cb=20160822213630" group-title="MEXICO",FORO TV
http://172.93.102.112:8989/158628_.m3u8
#EXTINF:-1 tvg-logo="https://noticias.nmas.com.mx/wp-content/themes/tvsa_noticias_ampnoamp/images/nmedia_logo_sqw.png" group-title="MEXICO",NMAS
https://linear-553.frequency.stream/dist/vix/dist/vix/553/hls/master/playlist.m3u8

#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/tv_azteca_corazon-mediano.png" group-title="NOVELAS",AZ CORAZÓN
http://tv.diablotv.net:8080/GeraJuvera25/m3uBHfmEeh/87568
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/tv_azteca_corazon-mediano.png" group-title="NOVELAS",AZ CORAZÓN OP2
https://stream.ads.ottera.tv/playlist.m3u8?network_id=4826
#EXTINF:-1 tvg-logo="https://televisa.brightspotcdn.com/7b/b9/1754376d4e608523ff87670c7a4e/canalu.png" group-title="NOVELAS",UNICABLE
http://tv.diablotv.net:8080/GeraJuvera25/m3uBHfmEeh/8382
#EXTINF:-1 tvg-logo="https://televisa.brightspotcdn.com/7b/b9/1754376d4e608523ff87670c7a4e/canalu.png" group-title="NOVELAS",UNICABLE HD
http://hidra.live:80/eufTQNgNXv/ojxBLcjY/302738
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/claros/pasiones-mediano.png" group-title="NOVELAS",PASIONES
http://200.115.120.1:8000/play/ca065/index.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/claros/pasiones-mediano.png" group-title="NOVELAS",PASIONES OP 2
http://tv.diablotv.net:8080/GeraJuvera25/m3uBHfmEeh/8335
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/claros/pasiones-mediano.png" group-title="NOVELAS",PASIONES OP 3
http://190.119.43.180:8000/play/a0zj/index.m3u8
#EXTINF:-1 tvg-logo="https://4.bp.blogspot.com/-E6b2VsJG0R4/UMeB36zPIhI/AAAAAAAAAoQ/byJ8wF8ArvI/s400/rojo.png" group-title="NOVELAS",TLNOVELAS 
http://190.60.37.154:45000/play/a01b/index.m3u8
#EXTINF:-1 tvg-logo="https://imgx.fubo.tv/station_logos/tlnovelas_m.png" group-title="NOVELAS",UNIVISION TLNOVELAS
http://user.tvcluboficial.com/live/u/u/413.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Telemundo_logo_2018.svg/1200px-Telemundo_logo_2018.svg.png" group-title="NOVELAS",TELEMUNDO INTERNACIONAL
http://190.119.43.180:8000/play/a0t6/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Telemundo_logo_2018.svg/1200px-Telemundo_logo_2018.svg.png" group-title="NOVELAS",TELEMUNDO INTERNACIONAL OP2 
http://tv14s.xyz:8080/FxQ7fU/508292/56410
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Telemundo_logo_2018.svg/1200px-Telemundo_logo_2018.svg.png" group-title="NOVELAS",TELEMUNDO PUERTO RICO  
http://190.60.37.154:45000/play/a03p/index.m3u8
#EXTINF:-1 tvg-logo="https://mondrian.claro.com.br/channels/inverse/tnt-novelas@3x.png" group-title="NOVELAS",TNT NOVELAS
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278403.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/LogoNTyLR.svg/1920px-LogoNTyLR.svg.png" group-title="NOVELAS",RCN  
http://190.60.37.154:45000/play/a04b/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/LogoNTyLR.svg/1920px-LogoNTyLR.svg.png" group-title="NOVELAS",RCN OP2
http://177.93.46.163:8000/play/a01o
#EXTINF:-1 tvg-logo="https://imagenes.canalrcn.com/ImgCanal/logos/rcntv-blanco-footer.png" group-title="NOVELAS",RCN INTERNACIONAL
http://disfruta.net:80/tvlatvgo152728e/XfnGyhSif8/257
#EXTINF:-1 tvg-logo="https://vignette.wikia.nocookie.net/logopedia/images/1/15/RCNnovelas2019.png" group-title="NOVELAS",RCN NOVELAS
http://168.227.22.18:1935/live/mobile-066/playlist.m3u8
#EXTINF:-1 tvg-logo="https://assets.my.rio2c.com/img/organizations/b6dad2d2-e711-4157-b64a-b22f0a07c791_thumbnail.png?v=20191202194516" group-title="NOVELAS",CARACOL
http://190.119.43.180:8000/play/a0ix/index.m3u8
#EXTINF:-1 tvg-logo="https://assets.my.rio2c.com/img/organizations/b6dad2d2-e711-4157-b64a-b22f0a07c791_thumbnail.png?v=20191202194516" group-title="NOVELAS",CARACOL OP2
http://190.60.37.154:45000/play/a04a/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/a/a1/Teletica_Logo.png" group-title="NOVELAS",TELETICA
http://zorrohunter.com:8080/haw5UJfF68/1729172230/46826
#EXTINF:-1 tvg-logo="https://media.telebajocero.com/adjuntos/225/imagenes/000/405/0000405645.jpg" group-title="NOVELAS",TELEFE
http://user.tvcluboficial.com/stream/m/m/771.m3u8
#EXTINF:-1 tvg-logo="https://media.telebajocero.com/adjuntos/225/imagenes/000/405/0000405645.jpg" group-title="NOVELAS",TELEFE OP2
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5de91cf02fc07c0009910465/master.m3u8?advertisingId=&appName=web&appVersion=5.4.0-03751b61882e52dadae8597d2215c0142b039e02&architecture=&buildVersion=&clientTime=&deviceDNT=0&deviceId=f354a4d6-9455-43c5-b1dc-937451d5c2fd&deviceMake=Android%20Browser&deviceModel=Android%20Browser&deviceType=web&deviceVersion=4.0&sid=0f6f4a5c-f300-43bc-b926-49419ffdc596

#EXTINF:-1 tvg-logo="https://cdn1.edgedatg.com/aws/v2/natgeo/Images/16c2ba84acf1e35302d1189a30433414/284x160-Q75_16c2ba84acf1e35302d1189a30433414.png" group-title="CULTURA",NACIONAL GEOGRAPHIC CHANNEL
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278637.m3u8
#EXTINF:-1 tvg-logo="https://cdn1.edgedatg.com/aws/v2/natgeo/Images/16c2ba84acf1e35302d1189a30433414/284x160-Q75_16c2ba84acf1e35302d1189a30433414.png" group-title="CULTURA",NAT GEO OP 2 
http://190.119.43.180:8000/play/a0s4/index.m3u8
#EXTINF:-1 tvg-logo="https://m.media-amazon.com/images/G/01/digital/video/merch/subs/benefit-id/g-l/lovenatureca/logos/3p-logo._CB436453497_.png" group-title="CULTURA",LOVE NATURE
http://nocable.cc:8080/live/PyRR3Hy/DthK0fv/47324.m3u8
#EXTINF:-1 tvg-logo="https://1000logos.net/wp-content/uploads/2020/09/Animal-Planet-Logo.png" group-title="CULTURA",ANIMAL PLANET
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278617.m3u8
#EXTINF:-1 tvg-logo="https://1000logos.net/wp-content/uploads/2020/09/Animal-Planet-Logo.png" group-title="CULTURA",ANIMAL PLANET OP2
http://190.119.43.180:8000/play/a0zf/index.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/history_latinoamerica-mediano.png" group-title="CULTURA",HISTORY MX
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/305646.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/history_latinoamerica-mediano.png" group-title="CULTURA",HISTORY PREMIUM 
http://190.119.43.180:8000/play/a0s8/index.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/history_2-mediano.png" group-title="CULTURA",H2
http://190.119.43.180:8000/play/a0zl/index.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/history_2-mediano.png" group-title="CULTURA",H2 OP2
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278629.m3u8
#EXTINF:-1 tvg-logo="https://logodownload.org/wp-content/uploads/2017/04/discovery-channel-logo-1.png" group-title="CULTURA",DISCOVERY CHANNEL MX
http://190.119.43.180:8000/play/a0w3/index.m3u8
#EXTINF:-1 tvg-logo="https://logodownload.org/wp-content/uploads/2017/04/discovery-channel-logo-1.png" group-title="CULTURA",DISCOVERY CHANNEL OP2
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278618.m3u8
#EXTINF:-1 tvg-logo="https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/4dff12f1-efa0-4572-ada1-cf3e8cc13ede/dfv40t2-20a89fb2-7f4e-45d1-8037-046a348a90dd.png/v1/fill/w_1280,h_422/discovery_family__nexus_potential_logo__by_subwooferlabs_dfv40t2-fullview.png?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7ImhlaWdodCI6Ijw9NDIyIiwicGF0aCI6IlwvZlwvNGRmZjEyZjEtZWZhMC00NTcyLWFkYTEtY2YzZThjYzEzZWRlXC9kZnY0MHQyLTIwYTg5ZmIyLTdmNGUtNDVkMS04MDM3LTA0NmEzNDhhOTBkZC5wbmciLCJ3aWR0aCI6Ijw9MTI4MCJ9XV0sImF1ZCI6WyJ1cm46c2VydmljZTppbWFnZS5vcGVyYXRpb25zIl19.i4J_wXHLaktlBFXd3QBpx03s8LVpAReGc3jDMi-qQQU" group-title="CULTURA",DISCOVERY FAMILY
http://super-iptv.live:80/110366811170D/162411098190/312557
#EXTINF:-1 tvg-logo="https://www.bellmedia.ca/lede/wp-content/uploads/2014/02/tv1_sci-white.png" group-title="CULTURA",DISCOVERY SCIENCE  
http://190.119.43.180:8000/play/a0v5/index.m3u8
#EXTINF:-1 tvg-logo="http://2.bp.blogspot.com/-J72Tsxq1uWo/VG1hwAAg98I/AAAAAAAAkDs/CP1NNsI_Ayk/s1600/_0040_discovery_hh.png" group-title="CULTURA",DISCOVERY H&H
http://190.119.43.180:8000/play/a0ws/index.m3u8
#EXTINF:-1 tvg-logo="http://2.bp.blogspot.com/-J72Tsxq1uWo/VG1hwAAg98I/AAAAAAAAkDs/CP1NNsI_Ayk/s1600/_0040_discovery_hh.png" group-title="CULTURA",DISCOVERY H&H HD
http://190.60.37.154:45000/play/a059/index.m3u8
#EXTINF:-1 tvg-logo="https://www.seekpng.com/png/full/240-2403856_hgtv-logo-hgtv-app.png" group-title="CULTURA",HGTV
http://00322.xyz:8000/live/saabj84/CNGUPDY6RA/4815.ts
#EXTINF:-1 tvg-logo="https://www.seekpng.com/png/full/240-2403856_hgtv-logo-hgtv-app.png" group-title="CULTURA",HGTV OP2
http://hdhd.tk:80/tvuser114/23Panthers19/645
#EXTINF:-1 tvg-logo="http://2.bp.blogspot.com/-050AUHplpUI/VFuobCREnsI/AAAAAAAAkCE/Zs_lGMgmA74/s1600/D.Turbo.png" group-title="CULTURA",DISCOVERY TURBO
http://190.60.37.154:45000/play/a04s/index.m3u8
#EXTINF:-1 tvg-logo="http://2.bp.blogspot.com/-050AUHplpUI/VFuobCREnsI/AAAAAAAAkCE/Zs_lGMgmA74/s1600/D.Turbo.png" group-title="CULTURA",DISCOVERY TURBO OP 2
http://200.59.188.3:8000/play/a045
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/8/85/World_Discovery_HD_logo.svg/1280px-World_Discovery_HD_logo.svg.png" group-title="CULTURA",DISCOVERY WORLD
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278625.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/8/85/World_Discovery_HD_logo.svg/1280px-World_Discovery_HD_logo.svg.png" group-title="CULTURA",DISCOVERY WORLD OP 2
http://190.119.43.180:8000/play/a0yr/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/1/18/Logo_id.png" group-title="CULTURA",DISCOVERY ID
http://190.119.43.180:8000/play/a0s6/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/1/18/Logo_id.png" group-title="CULTURA",DISCOVERY ID OP2
http://190.60.37.154:45000/play/a044/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/2015_Discovery_theater_HD_logo.svg/4096px-2015_Discovery_theater_HD_logo.svg.png" group-title="CULTURA",DISCOVERY THEATER
http://190.60.37.154:45000/play/a05g/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/2015_Discovery_theater_HD_logo.svg/4096px-2015_Discovery_theater_HD_logo.svg.png" group-title="CULTURA",DISCOVERY THEATER OP 2
http://190.119.43.180:8000/play/a0wt/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/TLC_USA_logo.png/150px-TLC_USA_logo.png" group-title="CULTURA",DISCOVERY TLC
http://209.126.106.30:8081/live/4757ada4db09488e8b909a71b9af3b8d/2ee168e8cc864d32baee88eed347be4b/49.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/TLC_USA_logo.png/150px-TLC_USA_logo.png" group-title="CULTURA",DISCOVERY TLC OP 2
http://38.43.150.125:8000/play/a022/index.m3u8

#EXTINF:-1 tvg-logo="https://3.bp.blogspot.com/-6TeTUuHgsxI/VrV2aR1efMI/AAAAAAAAGs0/f-yRhAXo-RU/s640/Tooncas_logo_jpg.png" group-title="CARTOONS",TOONCAST
http://tv.diablotv.net:8080/GeraJuvera25/m3uBHfmEeh/9883
#EXTINF:-1 tvg-logo="https://3.bp.blogspot.com/-6TeTUuHgsxI/VrV2aR1efMI/AAAAAAAAGs0/f-yRhAXo-RU/s640/Tooncas_logo_jpg.png" group-title="CARTOONS",TOONCAST OP 2
http://200.115.120.1:8000/play/ca090/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/d/dc/2016_Discovery_Kids_logo.svg/1164px-2016_Discovery_Kids_logo.svg.png" group-title="CARTOONS",DISCOVERY KIDS MX
http://190.119.43.180:8000/play/a0yu/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/d/dc/2016_Discovery_Kids_logo.svg/1164px-2016_Discovery_Kids_logo.svg.png" group-title="CARTOONS",DISCOVERY KIDS PREMIUM
http://190.60.37.154:45000/play/a035/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/8/85/Nick_%28Logo%29.png" group-title="CARTOONS",NICKELODEO
http://190.119.43.180:8000/play/a0w1/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/8/85/Nick_%28Logo%29.png" group-title="CARTOONS",NICK OP 2
http://hmbqa6.sekkd.com:80/live/amy5_y3zf4s/gnutz2k9/44.m3u8
#EXTINF:-1 tvg-logo="https://logodix.com/logo/73545.png" group-title="CARTOONS",NICK TEEN
http://177.53.153.20:8001/play/a0uq/index.m3u8
#EXTINF:-1 tvg-logo="https://logodix.com/logo/73545.png" group-title="CARTOONS",NICK TEEN OP2
http://190.61.102.67:8000/play/a04l
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/cartoon_network_latinoamerica-mediano.png" group-title="CARTOONS",CARTOON NETWORK
http://190.119.43.180:8000/play/a0wm/index.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/cartoon_network_latinoamerica-mediano.png" group-title="CARTOONS",CARTOON NETWORK HD
http://190.60.37.154:45000/play/a030/index.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/disney_channel-mediano.png" group-title="CARTOONS",DISNEY CHANNEL
http://190.60.37.154:45000/play/a03b/index.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/disney_channel-mediano.png" group-title="CARTOONS",DISNEY CHANNEL OP 2
http://190.119.43.180:8000/play/a0zt/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/en/thumb/4/42/Cartoonito_logo.svg/1280px-Cartoonito_logo.svg.png" group-title="CARTOONS",CARTOONITO
http://190.119.43.180:8000/play/a0zp/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/en/thumb/4/42/Cartoonito_logo.svg/1280px-Cartoonito_logo.svg.png" group-title="CARTOONS",CARTOONITO OP2
http://200.115.120.1:8000/play/ca006/index.m3u8
#EXTINF:-1 tvg-logo="http://comcab.mx/images/canales/canal_bitme_6838.png" group-title="CARTOONS",BIT ME
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278522.m3u8
#EXTINF:-1 tvg-logo="https://static.wikia.nocookie.net/logopedia/images/c/cf/Locomotion_2005.svg/revision/latest/scale-to-width-down/340?cb=20200125124642&path-prefix=es" group-title="CARTOONS",LOCOMOTION
http://51.222.85.85:81/hls/loco/index.m3u8?PlaylistM3UCL
#EXTINF:-1 tvg-logo="https://i.pinimg.com/originals/e8/3e/b9/e83eb97fac78e9328f8dd32ad2e527e7.png" group-title="CARTOONS",BABY TV
http://ott.cdn.iutpcdn.com/LIVE/H01/CANAL116/PROFILE03.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/nick_jr-mediano.png" group-title="CARTOONS",NICK JR
http://190.119.43.180:8000/play/a0us/index.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/oscuros/nick_jr-mediano.png" group-title="CARTOONS",NICK JR OP 2
http://190.60.37.154:45000/play/a04r/index.m3u8
#EXTINF:-1 tvg-logo="http://3.bp.blogspot.com/-VvbUqgHc5Pw/VJIJWAJLxfI/AAAAAAAAHi0/A0nDmO29zwk/s1600/Logo%2BDisney_Junior.png" group-title="CARTOONS",DISNEY JR
http://190.60.37.154:45000/play/a03a/index.m3u8
#EXTINF:-1 tvg-logo="http://3.bp.blogspot.com/-VvbUqgHc5Pw/VJIJWAJLxfI/AAAAAAAAHi0/A0nDmO29zwk/s1600/Logo%2BDisney_Junior.png" group-title="CARTOONS",DISNEY JR OP2
http://tv.diablotv.net:8080/GeraJuvera25/m3uBHfmEeh/9868
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/fr/thumb/5/5a/BabyFirst.svg/953px-BabyFirst.svg.png" group-title="CARTOONS",BABY FIRST
http://ott.cdn.iutpcdn.com/LIVE/H01/CANAL1984/PROFILE03.m3u8

#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/6/62/Hbo_blanco.png" group-title="ENTRETENIMIENTO PREMIUM",HBO
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278488.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/6/62/Hbo_blanco.png" group-title="ENTRETENIMIENTO PREMIUM",HBO OP2
http://51.79.81.69:8080/live/tVoIdcGo63/3k9huT771I/105.ts
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/claros/hbo_2_latinoamerica-mediano.png" group-title="ENTRETENIMIENTO PREMIUM",HBO2
http://51.79.81.69:8080/live/tVoIdcGo63/3k9huT771I/100.ts
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/claros/hbo_2_latinoamerica-mediano.png" group-title="ENTRETENIMIENTO PREMIUM",HBO2 OP2
http://190.119.43.180:8000/play/a0rf/index.m3u8
#EXTINF:-1 tvg-logo="https://i.pinimg.com/originals/c7/6f/24/c76f24d8dff0df01af6152f05ccb7a9c.png" group-title="ENTRETENIMIENTO PREMIUM",HBO PLUS
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278489.m3u8
#EXTINF:-1 tvg-logo="https://i.pinimg.com/originals/c7/6f/24/c76f24d8dff0df01af6152f05ccb7a9c.png" group-title="ENTRETENIMIENTO PREMIUM",HBO PLUS OP2
http://190.119.43.180:8000/play/a0zg/index.m3u8
#EXTINF:-1 tvg-logo="https://www.claro.com.co/portal/recursos/co/cpp/promociones/imagenes/1576097973446-HBO-xtreme-pequenio-b.png" group-title="ENTRETENIMIENTO PREMIUM",HBO XTREME
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278507.m3u8
#EXTINF:-1 tvg-logo="https://www.claro.com.co/portal/recursos/co/cpp/promociones/imagenes/1576097973446-HBO-xtreme-pequenio-b.png" group-title="ENTRETENIMIENTO PREMIUM",HBO XTREME OP2
http://190.119.43.180:8000/play/a0yh/index.m3u8
#EXTINF:-1 tvg-logo="https://www.claro.com.co/portal/recursos/co/cpp/promociones/imagenes/1576097892735-HBO-Family-pequenio.png" group-title="ENTRETENIMIENTO PREMIUM",HBO FAMILY
http://190.119.43.180:8000/play/a0rq/index.m3u8
#EXTINF:-1 tvg-logo="https://www.claro.com.co/portal/recursos/co/cpp/promociones/imagenes/1576097892735-HBO-Family-pequenio.png" group-title="ENTRETENIMIENTO PREMIUM",HBO FAMILY OP2
http://190.60.37.154:45000/play/a055/index.m3u8
#EXTINF:-1 tvg-logo="https://www.claro.com.co/portal/recursos/co/cpp/promociones/imagenes/1576097903824-HBO-mundi-pequenio-b.png" group-title="ENTRETENIMIENTO PREMIUM",HBO MUNDI
http://199.127.63.105:8989/278500_.m3u8 
#EXTINF:-1 tvg-logo="https://www.claro.com.co/portal/recursos/co/cpp/promociones/imagenes/1576097903824-HBO-mundi-pequenio-b.png" group-title="ENTRETENIMIENTO PREMIUM",HBO MUNDI OP2
http://play.flamaplus.tv:80/play/hUAC2XCgtW0N0759MiAJMF7XDvfyI18jH4u5BKWy8IA/ts
#EXTINF:-1 tvg-logo="https://www.foromedios.com/uploads/monthly_2020_02/large.1101503169_HBOPOP(varianteazul).png.681a6986dbf6d21eeea4d7efccea7e2a.png" group-title="ENTRETENIMIENTO PREMIUM",HBO POP
http://199.127.63.105:8989/278502_.m3u8
#EXTINF:-1 tvg-logo="https://www.foromedios.com/uploads/monthly_2020_02/large.1101503169_HBOPOP(varianteazul).png.681a6986dbf6d21eeea4d7efccea7e2a.png" group-title="ENTRETENIMIENTO PREMIUM",HBO POP OP2
http://play.flamaplus.tv:80/play/hUAC2XCgtW0N0759MiAJMMjzeLc7sN2qTL0Oi0Y0_ak/ts
#EXTINF:-1 tvg-logo="https://www.claro.com.co/portal/recursos/co/cpp/promociones/imagenes/1576097951981-HBO-signature-pequenio-b.png" group-title="ENTRETENIMIENTO PREMIUM",HBO SIGNATURE
http://2hubs.ddns.net:25461/iptvsolu/eI3mjZ1M7Y/62790
#EXTINF:-1 tvg-logo="https://www.universalplus.com/files/channels/780277389.png" group-title="ENTRETENIMIENTO PREMIUM",UNIVERSAL CRIME
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278474.m3u8
#EXTINF:-1 tvg-logo="https://www.universalplus.com/files/channels/120254749.png" group-title="ENTRETENIMIENTO PREMIUM",UNIVERSAL REALITY
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278478.m3u8
#EXTINF:-1 tvg-logo="https://www.universalplus.com/files/channels/398064723.png" group-title="ENTRETENIMIENTO PREMIUM",UNIVERSAL PREMIER
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278476.m3u8
#EXTINF:-1 tvg-logo="https://www.universalplus.com/files/channels/731381553.png" group-title="ENTRETENIMIENTO PREMIUM",UNIVERSAL CINEMA
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278472.m3u8
#EXTINF:-1 tvg-logo="https://www.universalplus.com/files/channels/172050409.png" group-title="ENTRETENIMIENTO PREMIUM",UNIVERSAL COMEDY
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278473.m3u8

#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/6/6b/DistritoComediaLOGO2018.png" group-title="ENTRETENIMIENTO",DISTRITO COMEDIA
http://200.115.98.26:9000/play/a01p/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/6/6b/DistritoComediaLOGO2018.png" group-title="ENTRETENIMIENTO",DISTRITO COMEDIA OP2
http://tv14s.xyz:8080/FxQ7fU/508292/57639a
#EXTINF:-1 tvg-logo="https://logodownload.org/wp-content/uploads/2021/05/comedy-central-logo-1.png" group-title="ENTRETENIMIENTO",COMEDY CENTRAL
http://200.115.120.1:8000/play/ca108/index.m3u8
#EXTINF:-1 tvg-logo="https://logodownload.org/wp-content/uploads/2021/05/comedy-central-logo-1.png" group-title="ENTRETENIMIENTO",COMEDY CENTRAL OP2
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278409.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/8/89/Star_Channel_2020.svg/270px-Star_Channel_2020.svg.png" group-title="ENTRETENIMIENTO",STAR CHANNEL HD
http://190.119.43.180:8000/play/a0yl/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/8/89/Star_Channel_2020.svg/270px-Star_Channel_2020.svg.png" group-title="ENTRETENIMIENTO",STAR CHANNEL HD OP2
http://190.60.37.154:45000/play/a02y/index.m3u8
#EXTINF:-1 tvg-logo="https://logodix.com/logo/587463.png" group-title="ENTRETENIMIENTO",FX HD
http://190.119.43.180:8000/play/a0vv/index.m3u8
#EXTINF:-1 tvg-logo="https://logodix.com/logo/587463.png" group-title="ENTRETENIMIENTO",FX HD OP2
http://190.60.37.154:45000/play/a05e/index.m3u8
#EXTINF:-1 tvg-logo="http://images.amcnetworks.com/sundancechannel.com/wp-content/uploads/2016/01/SundanceTV-logo-700x384.jpg" group-title="ENTRETENIMIENTO",SUNDANCE
http://lobo-tv.live:8080/Daleplayperu52/tVFeXytHnZ/2100619
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/Atreseries_2020_logo.svg/1200px-Atreseries_2020_logo.svg.png" group-title="ENTRETENIMIENTO",A3 SERIES
http://190.97.183.98:8000/play/a080/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/Atreseries_2020_logo.svg/1200px-Atreseries_2020_logo.svg.png" group-title="ENTRETENIMIENTO",A3 SERIES OP2
http://38.41.8.1:8000/play/a00x
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/b/b9/Sony_Channel_Logo.png" group-title="ENTRETENIMIENTO",SONY CHANNEL
http://190.119.43.180:8000/play/a0s5/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/b/b9/Sony_Channel_Logo.png" group-title="ENTRETENIMIENTO",SONY CHANNEL OP2
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278405.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/4/42/Universal_TV_logo.svg/1280px-Universal_TV_logo.svg.png" group-title="ENTRETENIMIENTO",UNIVERSAL HD
http://38.41.8.1:8000/play/a04r
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/4/42/Universal_TV_logo.svg/1280px-Universal_TV_logo.svg.png" group-title="ENTRETENIMIENTO",UNIVERSAL HD OP2
http://190.119.43.180:8000/play/a0sa/index.m3u8
#EXTINF:-1 tvg-logo="https://www.tvsa.co.za/images/uploads/thumbs/0057354.jpeg" group-title="ENTRETENIMIENTO",STUDIO UNIVERSAL
http://190.119.43.180:8000/play/a0xo/index.m3u8
#EXTINF:-1 tvg-logo="https://www.tvsa.co.za/images/uploads/thumbs/0057354.jpeg" group-title="ENTRETENIMIENTO",STUDIO UNIVERSAL OP2
http://190.60.37.154:45000/play/a038/index.m3u8
#EXTINF:-1 tvg-logo="http://img3.wikia.nocookie.net/__cb20130619050550/logopedia/images/thumb/2/27/Warner_Bros._wordless_banner.png/1157px-Warner_Bros._wordless_banner.png" group-title="ENTRETENIMIENTO",WARNER HD
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278453.m3u8
http://190.119.43.180:8000/play/a0zo/index.m3u8
#EXTINF:-1 tvg-logo="http://img3.wikia.nocookie.net/__cb20130619050550/logopedia/images/thumb/2/27/Warner_Bros._wordless_banner.png/1157px-Warner_Bros._wordless_banner.png" group-title="ENTRETENIMIENTO",WARNER HD OP2
http://190.119.43.180:8000/play/a0vx/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/b/bd/TNT-oficial.png" group-title="ENTRETENIMIENTO",TNT
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278519.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/b/bd/TNT-oficial.png" group-title="ENTRETENIMIENTO",TNT OP2
http://190.119.43.180:8000/play/a0zs/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/TNT_Serie.svg/649px-TNT_Serie.svg.png" group-title="ENTRETENIMIENTO",TNT SERIES
http://200.115.120.1:8000/play/ca035/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/TNT_Serie.svg/649px-TNT_Serie.svg.png" group-title="ENTRETENIMIENTO",TNT SERIES OP2
http://200.115.120.1:8000/play/ca035/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/SpaceLogo.svg/1024px-SpaceLogo.svg.png" group-title="ENTRETENIMIENTO",SPACE
http://190.60.37.154:45000/play/a04o/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/SpaceLogo.svg/1024px-SpaceLogo.svg.png" group-title="ENTRETENIMIENTO",SPACE OP2
http://190.119.43.180:8000/play/a0vu/index.m3u8
#EXTINF:-1 tvg-logo="https://th.bing.com/th/id/R.c98339a52309fce05f22a0b662130448?rik=PEEuqf5950bXyQ&riu=http%3a%2f%2fcablez.com.mx%2fwp-content%2fuploads%2f2017%2f03%2fPanico.png&ehk=ComIDC%2bW1CLDRnfsoOxzkytrCBfWOWHUx4%2fNIvrmLtA%3d&risl=&pid=ImgRaw&r=0" group-title="ENTRETENIMIENTO",PANICO
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278511.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/A%26E-Network-Logo.svg/1280px-A%26E-Network-Logo.svg.png" group-title="ENTRETENIMIENTO",A&E
http://190.60.37.154:45000/play/a03j/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/A%26E-Network-Logo.svg/1280px-A%26E-Network-Logo.svg.png" group-title="ENTRETENIMIENTO",A&E OP 2
http://190.119.43.180:8000/play/a0yq/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/AMC_logo_2016.svg/1920px-AMC_logo_2016.svg.png" group-title="ENTRETENIMIENTO",AMC
http://190.119.43.180:8000/play/a0l2/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/AMC_logo_2016.svg/1920px-AMC_logo_2016.svg.png" group-title="ENTRETENIMIENTO",AMC OP 2
http://tv14s.xyz:8080/FxQ7fU/508292/18925
#EXTINF:-1 tvg-logo="https://th.bing.com/th/id/R.d0e69673d5e442ae5e78911cfb415d18?rik=Ikf8YN%2bcXbY%2fVg&riu=http%3a%2f%2fpluspng.com%2fimg-png%2faxn-logo-vector-png-axn-logo-eps-vector-download-axn-450.png&ehk=nB3WTxrU%2fPrF572yLEuPgBxEOB3D6SR3Oi4RivKIVk4%3d&risl=&pid=ImgRaw&r=0" group-title="ENTRETENIMIENTO",AXN
http://190.119.43.180:8000/play/a0s9/index.m3u8
#EXTINF:-1 tvg-logo="https://th.bing.com/th/id/R.d0e69673d5e442ae5e78911cfb415d18?rik=Ikf8YN%2bcXbY%2fVg&riu=http%3a%2f%2fpluspng.com%2fimg-png%2faxn-logo-vector-png-axn-logo-eps-vector-download-axn-450.png&ehk=nB3WTxrU%2fPrF572yLEuPgBxEOB3D6SR3Oi4RivKIVk4%3d&risl=&pid=ImgRaw&r=0" group-title="ENTRETENIMIENTO",AXN OP 2
http://190.60.37.154:45000/play/a04n/index.m3u8
#EXTINF:-1 tvg-logo="https://logodownload.org/wp-content/uploads/2014/07/paramount-logo-0-1536x1536.png" group-title="ENTRETENIMIENTO",PARAMOUNT
http://tv.diablotv.net:8080/GeraJuvera25/m3uBHfmEeh/9708
#EXTINF:-1 tvg-logo="https://logodownload.org/wp-content/uploads/2014/07/paramount-logo-0-1536x1536.png" group-title="ENTRETENIMIENTO",PARAMOUNT OP 2
http://200.115.120.1:8000/play/ca115/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Cinemax_%28Yellow%29.svg/800px-Cinemax_%28Yellow%29.svg.png" group-title="ENTRETENIMIENTO",CINEMAX
http://190.119.43.180:8000/play/a0wk/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Cinemax_%28Yellow%29.svg/800px-Cinemax_%28Yellow%29.svg.png" group-title="ENTRETENIMIENTO",CINEMAX OP2
http://190.60.37.154:45000/play/a04l/index.m3u8
#EXTINF:-1 tvg-logo="https://iparatodos.com.ar/uploads/canal/imagenes/CINECANAL.png" group-title="ENTRETENIMIENTO",CINECANAL
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278462.m3u8
#EXTINF:-1 tvg-logo="https://iparatodos.com.ar/uploads/canal/imagenes/CINECANAL.png" group-title="ENTRETENIMIENTO",CINECANAL OP2
http://190.119.43.180:8000/play/a0vn/index.m3u8
#EXTINF:-1 tvg-logo="https://www.liblogo.com/img-logo/max/tc7455fdcb-tcm-logo-file-turner-classic-movies-tcm-latin-america-2015-logo-png.png" group-title="ENTRETENIMIENTO",TCM
http://200.59.188.3:8000/play/a05g
#EXTINF:-1 tvg-logo="https://www.liblogo.com/img-logo/max/tc7455fdcb-tcm-logo-file-turner-classic-movies-tcm-latin-america-2015-logo-png.png" group-title="ENTRETENIMIENTO",TCM OP2
http://190.97.183.98:8000/play/a0cb/index.m3u8
#EXTINF:-1 tvg-logo="https://www.retronyms.com/consulting/images/syfy-logo.png" group-title="ENTRETENIMIENTO",SYFY
http://190.119.43.180:8000/play/a0wf/index.m3u8
#EXTINF:-1 tvg-logo="https://www.retronyms.com/consulting/images/syfy-logo.png" group-title="ENTRETENIMIENTO",SYFY OP2
http://177.93.46.163:8000/play/a00i
#EXTINF:-1 tvg-logo="https://revistamomentos.co/wp-content/uploads/2017/08/Logo-Film-Arts-1.png" group-title="ENTRETENIMIENTO",FILM & ARTS
http://tv.diablotv.net:8080/GeraJuvera25/m3uBHfmEeh/9650
#EXTINF:-1 tvg-logo="https://revistamomentos.co/wp-content/uploads/2017/08/Logo-Film-Arts-1.png" group-title="ENTRETENIMIENTO",FILM & ARTS OP2
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278511.m3u8
http://190.119.43.180:8000/play/a0ya/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/E!_Logo.svg/560px-E!_Logo.svg.png" group-title="ENTRETENIMIENTO",E! OP2
http://177.93.46.163:8000/play/a012
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Logo_Golden_TV.svg/1200px-Logo_Golden_TV.svg.png" group-title="ENTRETENIMIENTO",GOLDEN
http://200.115.120.1:8000/play/ca024/index.m3u8
#EXTINF:-1 tvg-logo="https://2.bp.blogspot.com/-8yad3yUi75w/W6lpOlyQASI/AAAAAAAAAI4/jL-5Dh27kwgFNYlTyTXYYfeP7kBamdZkgCPcBGAYYCw/s1600/goldenedge.png" group-title="ENTRETENIMIENTO",GOLDEN EDGE
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278479.m3u8
#EXTINF:-1 tvg-logo="https://www.foromedios.com/uploads/monthly_2017_12/large.5a3bc10e723a2_GoldenPlus.png.e3f131696adc926862a7a22e2fa2f108.png" group-title="ENTRETENIMIENTO",GOLDEN PLUS
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278484.m3u8
#EXTINF:-1 tvg-logo="https://imagenes.gatotv.com/logos/canales/claros/golden_premier-mediano.png" group-title="ENTRETENIMIENTO",GOLDEN PREMIER
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278486.m3u8
#EXTINF:-1 tvg-logo="http://afiliados.televisa-networks.com/wp-content/uploads/2017/08/de-pelicula.png" group-title="ENTRETENIMIENTO",DE PELICULA
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278456.m3u8
#EXTINF:-1 tvg-logo="https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcRRMdFzHOZYEQAeqniX4n7Xn1cBQJKNMJoJiQ&usqp=CAU" group-title="ENTRETENIMIENTO",DE PELICULA PLUS
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278457.m3u8

#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/2/2b/TLHIT_logo.png/245px-TLHIT_logo.png" group-title="MUSICA",TELEHIT
http://200.115.120.1:8000/play/ca016/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/2/2b/TLHIT_logo.png/245px-TLHIT_logo.png" group-title="MUSICA",TELEHIT OP 2
http://tv.diablotv.net:8080/GeraJuvera25/m3uBHfmEeh/8315
#EXTINF:-1 tvg-logo="https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcS_eTwBAQAvlyG82g202STXCZ4jNt4_Zw7gWg&usqp=CAU" group-title="MUSICA",TELEHIT PLUS
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/3/3d/NickMusic_Logo.png" group-title="MUSICA",NICK MUSIC
http://187.188.232.236:1020/play/a00h/index.m3u8
#EXTINF:-1 tvg-logo="https://i1.wp.com/www.sopitas.com/wp-content/uploads/2020/04/VH1.jpeg" group-title="MUSICA",VH1 HD
http://tv.diablotv.net:8080/GeraJuvera25/m3uBHfmEeh/8320
#EXTINF:-1 tvg-logo="https://i1.wp.com/www.sopitas.com/wp-content/uploads/2020/04/VH1.jpeg" group-title="MUSICA",VH1 HD OP2
http://2hubs.ddns.net:25461/Iptr33/aQJScdWR8n9r/62673
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/a/ab/LOGO_VH1_CLASSIC.svg/1200px-LOGO_VH1_CLASSIC.svg.png" group-title="MUSICA",VH1 CLASSIC
http://200.115.120.1:8000/play/ca093/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/f/f8/MTV_Hits_2017_logo.svg/1200px-MTV_Hits_2017_logo.svg.png" group-title="MUSICA",MTV HITS
http://45.4.85.214:58001/play/a00z
#EXTINF:-1 tvg-logo="https://www.nicepng.com/png/full/57-579706_mtv-live-hd-mtv-live-logo.png" group-title="MUSICA",MTV LIVE
http://tv14s.xyz:8080/FxQ7fU/508292/53734
#EXTINF:-1 tvg-logo="https://www.nicepng.com/png/full/57-579706_mtv-live-hd-mtv-live-logo.png" group-title="MUSICA",MTV LIVE OP2
http://45.228.232.219:16000/play/a0m4/index.m3u8
#EXTINF:-1 tvg-logo="https://www.freepnglogos.com/uploads/mtv-png-logo/mtv-brand-png-logo-15.png" group-title="MUSICA",MTV
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278601.m3u8
#EXTINF:-1 tvg-logo="https://www.freepnglogos.com/uploads/mtv-png-logo/mtv-brand-png-logo-15.png" group-title="MUSICA",MTV OP 2
http://200.115.120.1:8000/play/ca086/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/MTV_Dance_2017_logo_Australia.svg/1200px-MTV_Dance_2017_logo_Australia.svg.png" group-title="MUSICA",MTV DANCE
http://unilivemtveu-lh.akamaihd.net/i/mtvno_1@346424/master.m3u8
#EXTINF:-1 tvg-logo="https://home-statics.boletia.com/uploads/sponsor/logo/130351/BANDAMAX.png" group-title="MUSICA",BANDAMAX
http://200.115.120.1:8000/play/ca84/index.m3u8
#EXTINF:-1 tvg-logo="https://forounivers.com/uploads/monthly_2021_09/large.1611336780_12.1-XHSAWTeleritmo.png.29062bd50ea09887c6c4e42cecfff260.png" group-title="MUSICA",TELERITMO
https://mdstrm.com/live-stream-playlist/57b4dc126338448314449d0c.m3u8
#EXTINF:-1 tvg-logo="https://th.bing.com/th/id/OIP.LEkqF9eTI4vb98MxJqnV3gEsDH?pid=ImgDet&rs=1" group-title="MUSICA",VIDEOROLA
https://d3b2epqdk0p7vd.cloudfront.net/out/v1/8a448b5e16384af4a3c8146a7b049c32/index_1.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/a/a3/Latinos_Up_TV_entretenimiento_online_en_New_York_y_el_mundo.png" group-title="MUSICA",LATINO UP
https://panel.streamingtv-mediacp.online:1936/8044/8044/playlist.m3u8?PlaylistM3UCL
#EXTINF:-1 tvg-logo="https://www.foromedios.com/uploads/monthly_2016_11/large.musictop_poster_-_copia.png.3681a31fef51ae9014cf64b2763b5f50.png" group-title="MUSICA",MUSICA TOP
http://live-edge01.telecentro.net.ar/live/msctphd-720/playlist.m3u8
#EXTINF:-1 tvg-logo="https://is4-ssl.mzstatic.com/image/thumb/Podcasts123/v4/5f/54/31/5f5431d3-60ad-8b79-effc-7d513e870630/mza_7191269111192758278.jpg/1200x1200bb.jpg" group-title="MUSICA",ON RADIO CHILE KPOP
https://tls-cl.cdnz.cl/adgm-2/live/playlist.m3u8
#EXTINF:-1 tvg-logo="http://www.topmusic.com.mx/img/music-club-logo-1466528095.jpg" group-title="MUSICA",TOP MUSIC
https://5cefcbf58ba2e.streamlock.net/tltvweb/tltvweb.stream/playlist.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/en/5/56/TeleXitos_TV.png" group-title="MUSICA",TVEXITOS
https://vdo1.streamgato.us:3359/live/tvexitoslive.m3u8?PlaylistM3UCL
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/pop-hqm-logo.png" group-title="MUSICA",MUSICA POP
https://livelist01.yowi.tv/lista/eb2fa68a058a701fa5bd2c80f6c8a6075896f71d/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/rock-hqm-logo.png" group-title="MUSICA",MUSICA ROCK
https://livelist01.yowi.tv/lista/0d6c7ccfac89946bfd41ae34c527e8d94734065c/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/hits-hqm-logo.png" group-title="MUSICA",MUSICA HITS
https://livelist01.yowi.tv/lista/5e2db2017a8fd03f73b40ede363d1a586db4e9a6/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/remember-hqm-logo.png" group-title="MUSICA",MUSICA REMEMBER
https://livelist01.yowi.tv:443/lista/57c98e2e295a0b69b52dc5f84edc4b1b68783ba2/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/relax-hqm-logo.png" group-title="MUSICA",MUSICA RELAX
https://livelist01.yowi.tv/lista/dc1b71c6fda2e687050facaa7242062cbf5a7f2a/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/latin-hqm-logo.png" group-title="MUSICA",MUSICA LATIN
https://livelist01.yowi.tv/lista/9a4da7871ec57b4b63ed49597a13d09869172be0/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/hip-hop-hqm-logo.png" group-title="MUSICA",MUSICA HIP HOT
https://livelist01.yowi.tv/lista/8327abc87895df4c76db1155435fdca6a3607bbd/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/gym-hqm-logo.png" group-title="MUSICA",MUSICA GYM
https://livelist01.yowi.tv/lista/abb87f329d0ed03072b1930e9636a53e8076c8d5/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/chill-out-hqm-logo.png" group-title="MUSICA",MUSICA CHILL OUT
https://livelist01.yowi.tv/lista/183a351ddb0e57af6d735256226e6033c32219ab/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/baladas-hqm-logo.png" group-title="MUSICA",MUSICA BALADAS
} https://livelist01.yowi.tv/lista/5d7d2c21e0ec7a8a99fd1fdbc52cbdc0782f77fc/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/folk-hqm-logo.png" group-title="MUSICA",MUSICA FLOK
https://livelist01.yowi.tv/lista/9f5310c179e8e840188d183be235f755b18cf703/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/dance-hqm-logo.png" group-title="MUSICA",MUSICA DANCE
https://livelist01.yowi.tv/lista/57cf2f51b07ff21988a7a6f0270a66d41086d4a4/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/arabic-hqm-logo-1.png" group-title="MUSICA",MUSICA ARABIC
https://livelist01.yowi.tv/lista/39596c72840d27b213caf4e58c39599a6f2ed203/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/spanish-hqm-logo.png" group-title="MUSICA",MUSICA SPANISH
https://livelist01.yowi.tv/lista/8635ae40f8d1a32eccd63d1f58b55662c9c98f9f/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/blues-hqm-logo.png" group-title="MUSICA",MUSICA BLUES
https://livelist01.yowi.tv/lista/81c601f370e44dc566113fd752204be5f5f53b61/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/classic-hqm-logo.png" group-title="MUSICA",MUSICA CLASSIC
https://livelist01.yowi.tv/lista/f04129475945936b248aa723de56519ea2ff10fc/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/jazz-hqm-logo.png" group-title="MUSICA",MUSICA JAZZ
https://livelist01.yowi.tv/lista/f204aa5b3f0691e69851b54b7746ef09ede26f6a/master.m3u8
#EXTINF:-1 tvg-logo="https://hqm.es/wp-content/uploads/kids-hqm-logo.png" group-title="MUSICA",MUSICA KIDS
https://livelist01.yowi.tv/lista/e4bc12dafe33c3ceb3e382e3acc0ec2c012cf7fd/master.m3u8
#EXTINF:-1 group-title="MUSICA" tvg-id="21" tvg-name="Retro Music Television" tvg-logo="https://lh3.googleusercontent.com/-z4SQcFR0bIs/Xs_MlInCKMI/AAAAAAAA1QM/T2RR2owl_CYpymBiQ8lOi1ykYZVAwI6NACK8BGAsYHg/s0/2020-05-28.png", Retro Music Television
http://stream.mediawork.cz/retrotv/smil:retrotv2.smil/playlist.m3u8?PlaylistM3UCL
#EXTINF:-1 group-title="MUSICA" tvg-id="24" tvg-name="Music Top" tvg-logo="https://lh3.googleusercontent.com/-6Em4fzTLMuU/XjtHLf96jvI/AAAAAAAAvqU/q3DfxbUdSiMDRDB1VBh_3hu3WDK_Nr-egCK8BGAsYHg/s0/2020-02-05.png", Music Top
http://live-edge01.telecentro.net.ar/live/smil:musictop.smil/playlist.m3u8?PlaylistM3UCL
#EXTINF:-1 group-title="MUSICA" tvg-id="17" tvg-name="The Retro Channel" tvg-logo="https://lh3.googleusercontent.com/-t2WD0yOsfvg/XzxnxYhCPPI/AAAAAAAA4oY/qwvkZXM7rAUzOACe0-fSRAQ7_8n6xb5wgCK8BGAsYHg/s0/2020-08-18.png", The Retro Channel
https://5df3045a6b70e.streamlock.net/theretrochannel/stream/playlist.m3u8?PlaylistM3UCL
#EXTINF:-1 group-title="MUSICA" tvg-id="42" tvg-name="RadioTV.pro" tvg-logo="https://lh3.googleusercontent.com/-wwOQH4WvSZI/X5jEE-BBCmI/AAAAAAAA6y4/RgkfOZHscEIH9ceLQR6UPtuXQQUw0FXCACK8BGAsYHg/s0/2020-10-27.png", RadioTV.pro
https://paneltv.online:1936/radiotv/radiotv/playlist.m3u8?PlaylistM3UCL
#EXTINF:-1 group-title="MUSICA" tvg-id="22" tvg-name="Video Tour Channel" tvg-logo="https://lh3.googleusercontent.com/-QUz4UviUPiE/X3YnewzIm7I/AAAAAAAA6Ho/xpUvyAvSX6Qj0Lvl8qhuZnDdz9VPZWy4QCK8BGAsYHg/s0/2020-10-01.png", Video Tour Channel
http://k4.usastreams.com/videotour/videotour/playlist.m3u8?PlaylistM3UCL

#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5dcb62e63d4d8f0009f36881/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV CINE ACCION
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5dcb62e63d4d8f0009f36881/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=397886a0-cd21-11eb-bd6e-0242ac110002&sid=397886a0-cd21-11eb-bd6e-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5e972a21ad709d00074195ba/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV ESTRELLAS DE ACCION
https://service-stitcher-ipv4.clusters.pluto.tv/v2/stitch/hls/channel/5e972a21ad709d00074195ba/3063648/playlist.m3u8?terminate=false&sid=ec3916f9-d8a2-11ed-b3fa-9ade4221ac90&deviceDNT=0&deviceLat=19.2663&deviceLon=-99.5828&deviceModel=web&deviceVersion=111.0.5563&includeExtendedEvents=true&paln=AQzzBGQEkIY6en3NFIKV4cLQdK3X9MEwEsM_AU0LOvSWyz8gC4lw-sdd6Q6K2JrezBe-bVwwwhCxql74vXpYcuRb1eml4Zj-O49uCtK01Lt6As7H9zN6BHgMk_F8pVBxSD9kL5nc0OwczGzZL3VWwml08AeuJvtX0cchCbOiZ-o96CBDANTDsf1kuyFAdZqO_6XUMmG8V18twX8OlqWGScrJ4yA9W0qO4qbqoquhWoq0u6YvdJyGqnxooeUd8IA848Uox73-LojS8WrpSIS4L4a5XH-gyXQNnNVV8wox5GWu1n56Rf_oWCUI65c_7IkTD-ShqWGxrxXB8h-zsI21cG99PGmXInGqhALSgSm9n8HYMhSoJXwS-awdO1TFwMmdTWsUV5CM0gersKWJ2PS8zhKBY8Se-lWsWejJSrkVFLMI-9dveFNFdGje93EwnaUPvZmmgKOlbmXuvsfGl6Zbwz8N1FkfK_9Zx5v0qYPlubVhPvzjA87XPERv98ry1Ap3tC8OfubKnJLx44NLq6LvJWEFQTycaeAU7qwoc_h2IG7X26V6-s4kB_GCSDn9KJmE_6_B8UGMIYbnj2coPJNI5JtYFM9Exu18QKo39RAB_xy_OmLbpYaf8VnbrJ-X9p0ZMIcR1qA43uPccsD9e9mhtjiERRupCuzHUDeJCJWTVq445mq1hJVNmwqVb-d30lkLjnyuqP_dPJFYyn1Y8Vy7WHIEHro3zXl1RA52WYaHOOjjtVsUv7O9qGkiUk0pHKCK0B_a50vVtiP3VB2FGiHr7Cqn9lWg0FD5Tjy3eYjTlBs-TBFhJYCAUI1kMhxrShLVXFaRSpPGYp6hprjE15kxNSMwgKoGzhS_K-07xHFq7PcqNzxdrJM165iDGDkdV53umM_XwjQvwSofORdYTf_yEKVfJY3yllPlmQfWrv1GV6-AG18t_RawzrlWBcSoimParkTqVl_vL_3vXrGXZVAD1oTnbjLlPOcW7uG2ftARtt_8Y0EOa69cd49wrmY_VCvIuHwM1Rc5EBMGZ87QRhF4EVcAaJbBNNCHw1sbFGC9cKtv6ndP4GqjZcwTAgpQ98WyIus8rJiNaQ6n8qYg_ypzZT8aPBcMfYchIXSJjh2cS0ptXSknD3eJ2dwtOEPfBMDW3VfItQ1681RsMf1JqdOlwIiadPgP3h6VXUPON9V-nnc0-T60T06zEbN01OQaBfeVYK5h0DugDaIAM71XwYpZ4n6RZ6H8qTlTFaDG8tPtHgFpOBEeIYhr-HgXqvsLAJnYHhJfWG_LWDo0ytkFSzyc2W_IcJnaMI8fX-Au-hV7WLdOkoBAfhe7CN7q9gJP1dxUzk9IXhJ9Fw3d8NWVBgoGY_qTyWglQnhDK-DwKyNo3Tzaa64_wDokyNdlgQQ5BiOSsbS4JCltPfaIzAlPtJU3u-zi9bxF7hKnrvnTXol1tLUXfPmzr2iq6MlWXJ7x6qzAMSp40yZNrkVQPZLCuUspLwy0RGdzj5hN0p0KB0mVG8fSFB5vga9dDUrs_HrQ7MGkYVO0tp3FZtdv4bfBGLV2oPD5bk19KZ1yDUUUynGWkPJwW5jTKSYnNHCz1opfcrxmGQk5-PE0F_ST-EQuAXB1-Gb_iYcfbKd_qC6K3j19fW-DI2aEa980l8Rl1633oitiCe0J_Suc8EPL2h1rMESCN_ubzZYxLkBpAa6DGK0eu-aO2rtqe3uW2g7MnqLtVIWjBHxMLmqRBfFXDwIUktCrnJFtDsXEeXSSSuE.&serverSideAds=true&appName=web&deviceId=0296d0b2-acef-4770-b9b5-65ea9c7a446c&appVersion=6.11.1-518d6dea293334a5d23e9fd9322e2bfc2744cda3&deviceType=web&deviceMake=chromium-webview&jwt=eyJhbGciOiJIUzI1NiIsImtpZCI6IjJhMzQ1MDFiLWE2NzItNGI3OC04NzYxLWY2ODMzNmZkYzViNiIsInR5cCI6IkpXVCJ9.eyJzZXNzaW9uSUQiOiJlYzM5MTZmOS1kOGEyLTExZWQtYjNmYS05YWRlNDIyMWFjOTAiLCJjbGllbnRJUCI6IjI4MDY6MTA0ZToxMzo4Mzk3OjRkZDplODAxOmMxOTc6NjIyOSIsImNpdHkiOiJNZXRlcGVjIiwicG9zdGFsQ29kZSI6IjUyMTcyIiwiY291bnRyeSI6Ik1YIiwiZG1hIjowLCJhY3RpdmVSZWdpb24iOiJWRSIsImRldmljZUxhdCI6MTkuMjY2MywiZGV2aWNlTG9uIjotOTkuNTgyOCwicHJlZmVycmVkTGFuZ3VhZ2UiOiJlbiIsImRldmljZVR5cGUiOiJ3ZWIiLCJkZXZpY2VWZXJzaW9uIjoiMTExLjAuNTU2MyIsImRldmljZU1ha2UiOiJjaHJvbWl1bS13ZWJ2aWV3IiwiZGV2aWNlTW9kZWwiOiJ3ZWIiLCJhcHBOYW1lIjoid2ViIiwiYXBwVmVyc2lvbiI6IjYuMTEuMS01MThkNmRlYTI5MzMzNGE1ZDIzZTlmZDkzMjJlMmJmYzI3NDRjZGEzIiwiY2xpZW50SUQiOiIwMjk2ZDBiMi1hY2VmLTQ3NzAtYjliNS02NWVhOWM3YTQ0NmMiLCJjbUF1ZGllbmNlSUQiOiIiLCJpc0NsaWVudEROVCI6ZmFsc2UsInVzZXJJRCI6IiIsImxvZ0xldmVsIjoiREVGQVVMVCIsInRpbWVab25lIjoiQW1lcmljYS9NZXhpY29fQ2l0eSIsInNlcnZlclNpZGVBZHMiOnRydWUsImUyZUJlYWNvbnMiOmZhbHNlLCJmZWF0dXJlcyI6e30sImlzcyI6ImJvb3QucGx1dG8udHYiLCJzdWIiOiJwcmk6djE6cGx1dG86ZGV2aWNlczpWRTpNREk1Tm1Rd1lqSXRZV05sWmkwME56Y3dMV0k1WWpVdE5qVmxZVGxqTjJFME5EWmoiLCJhdWQiOiIqLnBsdXRvLnR2IiwiZXhwIjoxNjgxMzI5Mzc3LCJpYXQiOjE2ODEyNDI5NzcsImp0aSI6IjRmODMzZWJmLTIzNWUtNDU1NS05NTQ2LWEwMzk1YTllN2U0MyJ9.SfeGtDyz_u1hs2p1KS3xtS81TgvIf_Kymy5lCidXcp8&CMCD=mtp%3D33600%2Cot%3Dm%2Csf%3Dh%2Csid%3D%223f0cb418-979b-4826-af30-28cd360caad1%22%2Csu
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5ddc4e8bcbb9010009b4e84f/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV CINE SUSPENSO
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5ddc4e8bcbb9010009b4e84f/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=f5b68be6-cd20-11eb-a259-0242ac110002&sid=f5b68be6-cd20-11eb-a259-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5dcddf1ed95e740009fef7ab/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV CINE TERROR
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5dcddf1ed95e740009fef7ab/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=7ad41cf4-cd21-11eb-85d6-0242ac110002&sid=7ad41cf4-cd21-11eb-85d6-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5dcde437229eff00091b6c30/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV CINE ESTELAR
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5dcde437229eff00091b6c30/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=ee36ed29-cd21-11eb-8734-0242ac110002&sid=ee36ed29-cd21-11eb-8734-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5defde6d6c07b50009cf0757/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV CINE
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5defde6d6c07b50009cf0757/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=342c0a11-cd22-11eb-b2c8-0242ac110002&sid=342c0a11-cd22-11eb-b2c8-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/609059dc63be6e0007b4eca6/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV CINE CLASICO
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/609059dc63be6e0007b4eca6/master.m3u8?advertisingId=&appName=web&appVersion=5.17.1-be7b5e79fc7cad022e22627cbb64a390ca9429c7&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&country=MX&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=abf84c6a-d2f6-11eb-96d3-0242ac110002&sid=abf84c6a-d2f6-11eb-96d3-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5de802659167b10009e7deba/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV SERIES RETRO
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5de802659167b10009e7deba/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=5069cb34-cd22-11eb-888c-0242ac110002&sid=5069cb34-cd22-11eb-888c-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/60142258a54aeb0007751c15/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV RETRO CARTOONS
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/60142258a54aeb0007751c15/master.m3u8?advertisingId=&appName=web&appVersion=5.106.0-fb1fedbd64193f9db000f97c44ce29de6b0bb9d5&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=5069cb34-cd22-11eb-888c-0242ac110002&sid=5069cb34-cd22-11eb-888c-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/6079bcbd9b10880007bf8e2d/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV RIPLEYS BELIEVE IT OR NOT
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/6079bcbd9b10880007bf8e2d/master.m3u8?advertisingId=&appName=web&appVersion=5.106.0-f3e2ac48d1dbe8189dc784777108b725b4be6be2&app_name=web&clientDeviceType=0&clientID=1d32070e-dae1-4f58-bd0d-f5c74f89cd0f&clientModelNumber=1.0.0&country=MX&deviceDNT=true&deviceId=1d32070e-dae1-4f58-bd0d-f5c74f89cd0f&deviceLat=19.2663&deviceLon=-99.5828&deviceMake=chrome&deviceModel=web&deviceType=web&deviceVersion=96.0.4664&marketingRegion=VE&serverSideAds=true&sessionID=e9e5ef8e-6604-11ec-a1cf-0242ac110003&sid=e9e5ef8e-6604-11ec-a1cf-0242ac110003&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5f4d3696d938c900072679fd/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV HISTORIAS DE ULTRATUMBA
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5f4d3696d938c900072679fd/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.6716&deviceLon=-98.8178&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=854d05b0-cd22-11eb-859f-0242ac110002&sid=854d05b0-cd22-11eb-859f-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5dcde2f53449c50009b2b4dc/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV MISTERIOS
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5dcde2f53449c50009b2b4dc/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=9e4540bb-cd22-11eb-8afd-0242ac110002&sid=9e4540bb-cd22-11eb-8afd-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5de5758e1a30dc00094fcd6c/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV HISTORIA
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5de5758e1a30dc00094fcd6c/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=b81abf51-cd22-11eb-859f-0242ac110002&sid=b81abf51-cd22-11eb-859f-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5dcde0cc2efd2700090b7ff4/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV NOVELAS
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5dcde0cc2efd2700090b7ff4/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=e505616f-cd22-11eb-a8b9-0242ac110002&sid=e505616f-cd22-11eb-a8b9-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5de91b7ea86ee60009d89e75/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV MTV CLASSIC
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5de91b7ea86ee60009d89e75/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=0460f0fa-cd23-11eb-859f-0242ac110002&sid=0460f0fa-cd23-11eb-859f-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5e98a911c881310007d7aae2/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV MTV PRANKS
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5e98a911c881310007d7aae2/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.6716&deviceLon=-98.8178&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=1fcf5748-cd23-11eb-8f91-0242ac110002&sid=1fcf5748-cd23-11eb-8f91-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5efb8ce2e426140007c78fd1/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV MTV ANIMACIONES
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5efb8ce2e426140007c78fd1/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=9f8a16fb-cd23-11eb-ad20-0242ac110002&sid=9f8a16fb-cd23-11eb-ad20-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5dd6d935d000120009bc1132/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV COMPETENCIAS
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5dd6d935d000120009bc1132/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=4130ed0c-cd23-11eb-8382-0242ac110002&sid=4130ed0c-cd23-11eb-8382-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5ed6828192e8b3000743ef61/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV WIPEOUT
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5ed6828192e8b3000743ef61/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=68155ab1-cd23-11eb-af82-0242ac110002&sid=68155ab1-cd23-11eb-af82-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5e8397936791b30007ebb5a7/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV HUMOR
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5e8397936791b30007ebb5a7/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=bf28769c-cd23-11eb-8497-0242ac110002&sid=bf28769c-cd23-11eb-8497-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5ffcc21a432945000762d06b/featuredImage.jpg?" group-title="PLUTO TV",PLUTO TV COMEDY CENTRAL
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5ffcc21a432945000762d06b/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=d33f16fe-cd23-11eb-8382-0242ac110002&sid=d33f16fe-cd23-11eb-8382-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5ddd7c348520b40009c347e2/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV NICK CLASSIC
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5ddd7c348520b40009c347e2/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=51de56d7-cd24-11eb-9091-0242ac110002&sid=51de56d7-cd24-11eb-9091-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5fab09a8749b1a00077d35d2/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV NICK TEENNICK
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5fab09a8749b1a00077d35d2/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=8b711b0f-cd24-11eb-bb4c-0242ac110002&sid=8b711b0f-cd24-11eb-bb4c-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5f4fed840a2764000720d966/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV NICK PAISAJES
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5f4fed840a2764000720d966/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=b402df8c-cd24-11eb-993e-0242ac110002&sid=b402df8c-cd24-11eb-993e-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5dd834c66fe2ca0009303b8d/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV NICK ANIMALES
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5dd834c66fe2ca0009303b8d/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=68ca370d-cd25-11eb-8a72-0242ac110002&sid=68ca370d-cd25-11eb-8a72-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/5f85ca40eda1b10007b967cd/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV NICK CONCIERTOS
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/5f85ca40eda1b10007b967cd/master.m3u8?advertisingId=&appName=web&appVersion=5.17.0-38a9908bb8d8f15260d990bd00c1f6b49c7bba28&app_name=web&clientDeviceType=0&clientID=4a6bef8a-83af-47b8-9507-be1f872bf780&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=0098c5c8-cd24-11eb-98a9-0242ac110002&sid=0098c5c8-cd24-11eb-98a9-0242ac110002&userId=
#EXTINF:-1 tvg-logo="https://images.pluto.tv/channels/6079be62f9f8d6000783c8a4/featuredImage.jpg" group-title="PLUTO TV",PLUTO TV ANIMES HEROES
https://service-stitcher.clusters.pluto.tv/stitch/hls/channel/6079be62f9f8d6000783c8a4/master.m3u8?advertisingId=&appName=web&appVersion=5.106.0-f3e2ac48d1dbe8189dc784777108b725b4be6be2&app_name=web&clientDeviceType=0&clientID=1d32070e-dae1-4f58-bd0d-f5c74f89cd0f&clientModelNumber=na&deviceDNT=true&deviceId=4a6bef8a-83af-47b8-9507-be1f872bf780&deviceLat=19.2474&deviceLon=-99.5671&deviceMake=Android%2BBrowser&deviceModel=web&deviceType=web&deviceVersion=4.0&marketingRegion=VE&serverSideAds=true&sessionID=0098c5c8-cd24-11eb-98a9-0242ac110002&sid=0098c5c8-cd24-11eb-98a9-0242ac110002&userId=

#EXTINF:-1 tvg-logo="https://3.bp.blogspot.com/-TNq_WQvu70w/WVrJrd2fhWI/AAAAAAAA8KQ/J02VFD8ys-sKxshHsgbLuXVBJIH_IYlawCLcBGAs/s640/espnextra.png" group-title="DEPORTES",ESPN EXTRA 
http://200.115.98.26:9000/play/a044/index.m3u8
#EXTINF:-1 tvg-logo="https://3.bp.blogspot.com/-TNq_WQvu70w/WVrJrd2fhWI/AAAAAAAA8KQ/J02VFD8ys-sKxshHsgbLuXVBJIH_IYlawCLcBGAs/s640/espnextra.png" group-title="DEPORTES",ESPN EXTRA OP2
http://45.5.117.182:8000/play/a0kg/index.m3u8
#EXTINF:-1 tvg-logo="https://logos-download.com/wp-content/uploads/2016/05/ESPN_logo_wordmark.png" group-title="DEPORTES",ESPN
http://xcm.budtv-ultra.com:9000/budtv/budtvline/8Y3ymUpJESps/278578.m3u8
#EXTINF:-1 tvg-logo="https://logos-download.com/wp-content/uploads/2016/05/ESPN_logo_wordmark.png" group-title="DEPORTES",ESPN OP 2
http://181.188.234.21:8000/play/a041/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/b/bf/ESPN2_logo.svg/1200px-ESPN2_logo.svg.png" group-title="DEPORTES",ESPN 2
http://190.60.37.154:45000/play/a039/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/b/bf/ESPN2_logo.svg/1200px-ESPN2_logo.svg.png" group-title="DEPORTES",ESPN 2 OP 2
http://190.119.43.180:8000/play/a0yc/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/5/51/ESPN3_Logo.png" group-title="DEPORTES",ESPN 3
http://190.119.43.180:8000/play/a0ye/47753062.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/5/51/ESPN3_Logo.png" group-title="DEPORTES",ESPN 3 OP 2
http://190.60.37.154:45000/play/a04u/index.m3u8
#EXTINF:-1 tvg-logo="https://forounivers.com/uploads/monthly_2021_12/large.1522528359_ESPN4_Blanco.png.94baa236c27d1b47d48584df19f07993.png" group-title="DEPORTES",ESPN 4
http://177.93.46.163:8000/play/a01j
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/d/db/ESPN_Premium_logo.png" group-title="DEPORTES",ESPN SPORTS PREMIUM
http://190.119.43.180:8000/play/a0yz/index.m3u8
#EXTINF:-1 tvg-logo="https://4.bp.blogspot.com/-7tQNOR4oaRg/WOVNdl6IBkI/AAAAAAACso0/fu-_vwRbVN8HqN4x9RNEFgJFI8seZTHKwCLcB/s1600/cosas.jpg" group-title="DEPORTES",FOX SPORTS PREMIUM
http://199.127.63.105:8989/278598_.m3u8
#EXTINF:-1 tvg-logo="https://clipground.com/images/fox-sports-logo-png-2.png" group-title="DEPORTES",FOX SPORTS
http://199.127.63.105:8989/278595_.m3u8
#EXTINF:-1 tvg-logo="https://clipground.com/images/fox-sports-logo-png-2.png" group-title="DEPORTES",FOX SPORTS OP 2
http://daleplay.club:8080/live/NNE5379khh/playec356nm/fc00ed7b-a8a6-4727-9b02-c69226282aa3.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/en/7/7e/Fox_Sports_2_logo.png" group-title="DEPORTES",FOX SPORT 2
http://199.127.63.105:8989/278590_.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/en/7/7e/Fox_Sports_2_logo.png" group-title="DEPORTES",FOX SPORT 2 OP 2
http://199.127.63.105:8989/278590_.m3u8
#EXTINF:-1 tvg-logo="https://i.pinimg.com/originals/b3/08/8c/b3088cdf394935e26f9e9da5be7cd946.png" group-title="DEPORTES",FOX SPORT 3
http://190.60.37.154:45000/play/a03o/index.m3u8
#EXTINF:-1 tvg-logo="https://i.pinimg.com/originals/b3/08/8c/b3088cdf394935e26f9e9da5be7cd946.png" group-title="DEPORTES",FOX SPORT 3 OP 2
http://190.60.37.154:45000/play/a03o/index.m3u8
#EXTINF:-1 tvg-logo="https://image.roku.com/developer_channels/prod/f7414ac0674bae38158d34fd5fde3630f8e3be01aa74c4ba9168d1ca52450214.png" group-title="DEPORTES",FUTBOL NETWORK
https://59d39900ebfb8.streamlock.net:443/8312/8312/playlist.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/DirecTV_Sports_Latin_America_-_2018_logo_v2.svg/1719px-DirecTV_Sports_Latin_America_-_2018_logo_v2.svg.png" group-title="DEPORTES",DIRECTV SPORTS
http://user.tvcluboficial.com/live/m/m/259.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/DirecTV_Sports_Latin_America_-_2018_logo_v2.svg/1719px-DirecTV_Sports_Latin_America_-_2018_logo_v2.svg.png" group-title="DEPORTES",DIRECTV SPORTS 2
http://user.tvcluboficial.com/live/m/m/667.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/DirecTV_Sports_Latin_America_-_2018_logo_v2.svg/1719px-DirecTV_Sports_Latin_America_-_2018_logo_v2.svg.png" group-title="DEPORTES",DIRECTV SPORTS PLUS
http://user.tvcluboficial.com/live/m/m/935.m3u8
#EXTINF:-1 tvg-logo="http://img4.wikia.nocookie.net/__cb20120805063727/logopedia/images/thumb/9/98/Univision_tdn_1.png/500px-Univision_tdn_1.png" group-title="DEPORTES",UNIVISION TUDN
http://tv14s.xyz:8080/FxQ7fU/508292/11885
#EXTINF:-1 tvg-logo="https://www.pandaancha.mx/plds/articulos/froala/AFIZZIONADOS-201x200-925511197-201x200-593359461.png" group-title="DEPORTES",AFIZZIONADOS
http://offshore.lat:8080/changeESUS@/7QcXgwrCWfYv/269758.ts
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/6/6c/TUDN_Logo.svg/800px-TUDN_Logo.svg.png" group-title="DEPORTES",TUDN HD
http://user.tvcluboficial.com/live/m/m/698.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/a/ad/GOLTV.png" group-title="DEPORTES",GOLTV
http://200.115.98.26:9000/play/a00q/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/a/ad/GOLTV.png" group-title="DEPORTES",GOLTV 2
http://181.78.105.146:2000/play/a094/index.m3u8
#EXTINF:-1 tvg-logo="https://www.foromedios.com/uploads/monthly_2021_01/large.logo_tntsports_hd.png.2de3e07868016506434e4a6b957ca56e.png" group-title="DEPORTES",TNT SPORTS
http://200.115.120.1:8000/play/ca040/index.m3u8
#EXTINF:-1 tvg-logo="https://www.foromedios.com/uploads/monthly_2021_01/large.logo_tntsports_hd.png.2de3e07868016506434e4a6b957ca56e.png" group-title="DEPORTES",TNT SPORTS HD
http://200.113.4.75:7210/play/a011/index.m3u8
#EXTINF:-1 tvg-logo="https://www.nicepng.com/png/full/419-4196216_cdo-premium-canal-cdo.png" group-title="DEPORTES",CANAL DEL DEPORTE OLIMPICO
http://streamsy.online:2999/happy2024/newyear/1707
#EXTINF:-1 tvg-logo="https://www.megacable.com.mx/images/canales/673_tvc-deportes.png" group-title="DEPORTES",TVC DEPORTES
http://ott.cdn.iutpcdn.com/LIVE/H01/CANAL2157/PROFILE03.m3u8
#EXTINF:-1 tvg-logo="https://pescaendirecto.com/wp-content/uploads/easy_logo_slider/498508Logo-AYM.png" group-title="DEPORTES",AYM SPORTS
http://streamsy.online:2999/happy2024/newyear/2873
#EXTINF:-1 tvg-logo="https://pescaendirecto.com/wp-content/uploads/easy_logo_slider/498508Logo-AYM.png" group-title="DEPORTES",AYM SPORTS OP2
http://ott.cdn.iutpcdn.com/LIVE/H01/CANAL576/PROFILE03.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Claro_Sports_logo.svg/1280px-Claro_Sports_logo.svg.png" group-title="DEPORTES",CLARO SPORTS
http://tecnozone.site:8880/live/Danielhy829/Daniel-829h/68.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Claro_Sports_logo.svg/1280px-Claro_Sports_logo.svg.png" group-title="DEPORTES",CLARO SPORTS OP2
http://45.167.3.81:45000/play/a00h/index.m3u8
#EXTINF:-1 tvg-logo="https://www.foromedios.com/uploads/monthly_2016_10/large.ClaroSports2.png.56053360e8aac876562fd738f2b7ff59.png" group-title="DEPORTES",CLARO SPORTS 2
http://45.167.3.81:45000/play/a00i/index.m3u8
#EXTINF:-1 tvg-logo="http://afiliados.televisa-networks.com/wp-content/uploads/2018/08/adrenalina-sports.network.png" group-title="DEPORTES",ADRENALINA SPORTS HD
http://104.238.205.28:8989/278573_.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/f/f1/Movistar_Deportes_1.svg/1200px-Movistar_Deportes_1.svg.png" group-title="DEPORTES",MOVISTAR SPORTS
http://offshore.lat:8080/changeESUS@/7QcXgwrCWfYv/1264.ts
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/Movistar_LaLiga.svg/1200px-Movistar_LaLiga.svg.png" group-title="DEPORTES",MOVISTAR LA LIGA
http://streamsy.online:2999/happy2024/newyear/1774
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/0/03/WinSports2017.png" group-title="DEPORTES",WIN SPORTS
http://zorrohunter.com:8080/haw5UJfF68/1729172230/46835
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b0/Win_Sports%2B_logo.svg/200px-Win_Sports%2B_logo.svg.png" group-title="DEPORTES",WIN SPORTS PLUS
http://streamsy.online:2999/happy2024/newyear/1417
#EXTINF:-1 tvg-logo="https://i.pinimg.com/originals/11/b1/48/11b14885c76a7e6534130043e86337d0.png" group-title="DEPORTES",BEIN SPORTS
http://xcm.budtv-ultra.com:25461/live/bud-185308/acbd5173b7/50421.m3u8
#EXTINF:-1 tvg-logo="https://th.bing.com/th/id/R.d9e08c3205afcf0a6a2a9b91f1b849ef?rik=SXhZ7dSfPiECaw&riu=http%3a%2f%2fandabolivia.com%2fwp-content%2fuploads%2f2018%2f05%2ftigo-sports.png&ehk=enXRtyb%2bSKMEquecsDcEbATCWPgbcqEe0YEqdg4NlaM%3d&risl=&pid=ImgRaw&r=0" group-title="DEPORTES",TIGO SPORTS
http://45.5.117.172:8000/play/a02v/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/5/54/TyC_Sports_logo.svg/1280px-TyC_Sports_logo.svg.png" group-title="DEPORTES",TYCSPORTS
http://45.166.92.22:58001/play/a04d/index.m3u8

#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/Movistar_Acci%C3%B3n.svg/640px-Movistar_Acci%C3%B3n.svg.png" group-title="MOVISTAR",MOVISTAR ACCION
http://tv.proyectox.vip:8080/ossi2022/RNe48QBDyX/117233
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/MovistarCine%C3%91.png/1200px-MovistarCine%C3%91.png" group-title="MOVISTAR",MOVISTAR CINE
http://tv.proyectox.vip:8080/ossi2022/RNe48QBDyX/117337
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/d/da/Movistar_Series.svg/1200px-Movistar_Series.svg.png" group-title="MOVISTAR",MOVISTAR SERIES
http://tv.proyectox.vip:8080/ossi2022/RNe48QBDyX/117242
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Movistar_Estrenos.svg/1280px-Movistar_Estrenos.svg.png" group-title="MOVISTAR",MOVISTAR ESTRENOS
http://tv.proyectox.vip:8080/ossi2022/RNe48QBDyX/117242
#EXTINF:-1 tvg-logo="https://vignette1.wikia.nocookie.net/logopedia/images/e/e4/MComedia.png/revision/latest?cb=20160707185637" group-title="MOVISTAR",MOVISTAR COMEDIA
http://tv.proyectox.vip:8080/ossi2022/RNe48QBDyX/117238

#EXTINF:-1 tvg-logo="https://static.wikia.nocookie.net/logopedia/images/c/cb/Gol_Per%C3%BA_2016.svg/revision/latest/scale-to-width-down/340?cb=20200417023402&path-prefix=es" group-title="PERU",GOL PERU
https://castlempire.site:443/esteb5982roj/CchZQLdGB3/256890.ts
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/ATV_logo_2020.png/640px-ATV_logo_2020.png" group-title="PERU",ATV
https://miap.xyz/224/serverkey.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/c/cb/Logo_TV_Per%C3%BA.png" group-title="PERU",TV PERU
http://144.126.128.123:25461/manoapoyanos/shortgod.com/11626
#EXTINF:-1 tvg-logo="https://appnew.megainfoapp.com/files/1/ovaciontv.png" group-title="PERU",OVACION
http://cdn2.ujjina.com:1935/iptvovacion1/liveovacion1tv/playlist.m3u8
#EXTINF:-1 tvg-logo="https://i2.wp.com/directostv.teleame.com/wp-content/uploads/2017/10/Radio-Turbo-Mix-TV-en-vivo-Online.png?fit=1920%2C1080" group-title="PERU",CENTRAL TV
http://167.114.116.212:1935/tvmb/turbomix/master.m3u8
#EXTINF:-1 tvg-logo="https://www.radionacional.com.pe/sites/default/files/logo_tvperu_noticias.png" group-title="PERU",TV PERU NOTICAS
http://cdnh4.iblups.com/hls/RMuwrdk7M9.m3u8
#EXTINF:-1 tvg-logo="https://www.pngkey.com/png/detail/345-3452968_latina-latina-peru-logo-png.png" group-title="PERU",LATINA PERU
https://kokoapps.xyz/ch2.php?ch=latina.html&f=.m3u8
#EXTINF:-1 tvg-logo="https://i1.wp.com/directostv.teleame.com/wp-content/uploads/2017/10/BHTV-en-vivo-Online.png?fit=1920%2C1080" group-title="PERU",BHTV
http://cdn2.ujjina.com:1935/iptvbhtv/livebhtvtv/playlist.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/1/1c/Cultura_24_logo.png" group-title="PERU",24 TV CULTURALES
http://vs8.live.opencaster.com/cultura24/smil:cultura24/chunklist_w450881441_b964000_sles.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/e/ee/Canal_IPe_Logo_PNG.png" group-title="PERU",IPe
http://l3d.us:8080/4Lxx4KOD/WHGnYwuGFs/155855
#EXTINF:-1 tvg-logo="https://static.wikia.nocookie.net/logopedia/images/4/4a/Canal_N_1999.svg/revision/latest/scale-to-width-down/340?cb=20200822010746&path-prefix=es" group-title="PERU",CANAL N
http://181.198.239.34:58003/play/a014/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/6/6d/Panamericana-tv.svg/1200px-Panamericana-tv.svg.png" group-title="PERU",PANAMERICANA TV
http://l3d.us:8080/4Lxx4KOD/WHGnYwuGFs/10628
#EXTINF:-1 tvg-logo="https://www.radionacional.com.pe/sites/default/files/logo_tvperu.png" group-title="PERU",TYPE
https://miap.xyz/235/serverkey.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/4/49/Logoexitosa.png" group-title="PERU",EXITOSA
https://cu.onliv3.com/livevd1/user2.m3u8

#EXTINF:-1 tvg-logo="https://cdn-profiles.tunein.com/s292162/images/logog.png?t=636314" group-title="NOTICIAS",CNN INTERNACIONAL
http://200.115.120.1:8000/play/ca045/index.m3u8
#EXTINF:-1 tvg-logo="https://cdn-profiles.tunein.com/s292162/images/logog.png?t=636314" group-title="NOTICIAS",CNN INTERNACIONAL OP2
http://181.189.243.217:8000/play/a0o7/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Logo_de_CNN_en_Espa%C3%B1ol_%282010-2015%29.svg/1200px-Logo_de_CNN_en_Espa%C3%B1ol_%282010-2015%29.svg.png" group-title="NOTICIAS",CNN EN ESPAÑOL
http://200.115.120.1:8000/play/ca044/index.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Logo_de_CNN_en_Espa%C3%B1ol_%282010-2015%29.svg/1200px-Logo_de_CNN_en_Espa%C3%B1ol_%282010-2015%29.svg.png" group-title="NOTICIAS",CNN EN ESPAÑOL OP2
http://181.189.243.217:8000/play/a0ps/index.m3u8
#EXTINF:-1 tvg-logo="http://directostv.teleame.com/wp-content/uploads/2017/10/CNN-Chile-en-vivo-Online-1.png" group-title="NOTICIAS",CNN CHILE
http://170.83.242.9:4000/play/a036
#EXTINF:-1 tvg-logo="https://tvazteca.brightspotcdn.com/03/74/823bf90340f682d157fcbf4bae48/logo-512x142-adn40-a001.png" group-title="NOTICIAS",ADN40
http://vlmain.hopto.org:25461/TuxChannel/9ee9g38Dj5ApATuypXTWJfBS/38
#EXTINF:-1 tvg-logo="https://tvazteca.brightspotcdn.com/03/74/823bf90340f682d157fcbf4bae48/logo-512x142-adn40-a001.png" group-title="NOTICIAS",ADN40 OP2 
http://ott.cdn.iutpcdn.com/LIVE/H01/CANAL2/PROFILE03.m3u8
#EXTINF:-1 tvg-logo="https://w7.pngwing.com/pngs/897/553/png-transparent-bloomberg-terminal-management-company-bloomberg-law-fresh-and-meaty-miscellaneous-company-text.png" group-title="NOTICIAS",BLOOMBERG
http://51.222.43.170:8001/cristiancl/cirtianiptv/282744
#EXTINF:-1 tvg-logo="https://w7.pngwing.com/pngs/897/553/png-transparent-bloomberg-terminal-management-company-bloomberg-law-fresh-and-meaty-miscellaneous-company-text.png" group-title="NOTICIAS",BLOOMBERG OP2 
http://ott.cdn.iutpcdn.com/LIVE/H01/CANAL190/PROFILE03.m3u8
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/2/2c/Logotipo_de_24_Horas_TVN.png" group-title="NOTICIAS",24HORAS
http://51.222.43.170:8001/cristiancl/cirtianiptv/282467
#EXTINF:-1 tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/Milenio_%282018-10-27%29.svg/1200px-Milenio_%282018-10-27%29.svg.png" group-title="NOTICIAS",MILENIO
http://vlmain.hopto.org:25461/TuxChannel/9ee9g38Dj5ApATuypXTWJfBS/33
`  """.trimIndent()

    fun getChannels(): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = M3U_SOURCE.split("\n")
        var currentName = ""
        var currentLogo = ""
        var currentGroup = ""

        for (line in lines) {
            if (line.startsWith("#EXTINF")) {
                currentName = line.substringAfterLast(",").trim()
                currentLogo = line.substringAfter("tvg-logo=\"", "").substringBefore("\"", "")
                currentGroup = line.substringAfter("group-title=\"", "").substringBefore("\"", "")
            } else if (line.startsWith("http")) {
                channels.add(Channel(currentName, line.trim(), currentLogo, currentGroup))
            }
        }
        return channels
    }
}