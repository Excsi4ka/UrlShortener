alter table click_analytics
add constraint fk_click_analytics_url
foreign key (short_url)
references urls(short_url);