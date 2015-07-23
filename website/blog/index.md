---
layout: default
title: Blog
---

<main id="main" >
<section id="first" class="main">
    <header style="padding:2em 0 4em 0;">
      <div class="container" >
        <h4 class="section-title"><span> Kylin Technical Blog </span></h4>
         <!-- second-->
          <div id="content-container" class="animated fadeIn">
            <div >
            <!--
            <p class="aboutkylin" style="font-size:1.2em">Comming Soon...</p>
            -->
             <ul class="post-list">
            {% for category in site.categories %}     <!-- categories -->
            {% if category[0]  == 'blog' %}
            {% for post in category[1] %}
            <li>
            <!--
            <span align="left" class="content-header">{{ post.date | date: "%b %-d, %Y" }}</span>
            -->
        <h2 align="left">
          <a class="post-link" href="{{ post.url | prepend: site.baseurl }}">{{ post.title }}</a>
        </h2>
      </li>
    {% endfor %}
    {% endif %}
    {% endfor %}
  </ul>

  <p class="rss-subscribe">subscribe <a href="{{ "/feed.xml" | prepend: site.baseurl }}">via RSS</a></p>
          </div>
        </div>
      </div>
      <!-- /container --> 
      
    </header>
  </section>

  
    
</main>
