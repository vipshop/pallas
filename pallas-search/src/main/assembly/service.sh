#!/bin/bash
cd /apps/dat/web/working/${VIP_PALLAS_SEARCH_DOMAIN:='pallas-search.api.vip.com'}
sh bin/pallas-search.sh $@ -p 8080