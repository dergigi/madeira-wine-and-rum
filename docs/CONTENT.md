# Guide content

Editorial content lives in `app/src/main/assets/guide.json`. The app includes
source links on articles, place details and bottle suggestions. Sources were
reviewed on 2026-10-23; this is a bundled edition, not a live availability feed.

Recommendations and the seven-day sequence are editorial choices. Producer
websites, Visit Madeira, IVBAM and the regional rum directory support production
facts, visit options and bottle names. Taberna da Poncha additionally uses the
All About Portugal venue listing; Casa do Povo da Serra de Água documents the
community's poncha festival. Do not describe these places as undiscovered or
claim a local consensus about the best drink.

When updating content:

- Verify the source page and revise the review date alongside changed facts.
- Keep visit facts distinct from suggestions about what to ask for.
- Avoid fixed prices, opening hours or promises of stock without current checks.
- Preserve the distinction between fortified Madeira Wine and Madeiran still wine.
- Update related day plans and bottle suggestions when changing a venue.
- Keep `id` values stable: saved places use those keys in local preferences.

## Map

`coastline.json` contains the Madeira polygon extracted from Natural Earth's
1:10m admin-0 country boundaries, Portugal feature. Original data:
https://github.com/nvkelso/natural-earth-vector/blob/master/geojson/ne_10m_admin_0_countries.geojson

Natural Earth data is public domain:
https://www.naturalearthdata.com/about/terms-of-use/

Place coordinates are approximate area markers. The UI explicitly says so.
They are not used for navigation; Google Maps receives venue names and addresses.
The coastline is an island overview rather than a street map. An accessible
numbered list mirrors every filtered pin, and overlapping pins offer a chooser.

The map, filters, content and saved places work without network access. Google
Maps, booking pages and source links open in an external application. The app
itself requests neither internet nor location permission.

## Visual browsing

Since 0.3.0, the main tabs present imagery and short captions. Full articles remain
in `guide.json` and are shown through story dialogs and expandable sections;
place tips, source links and itinerary transport notes are retained in details.
`media.json` holds photograph credits and curated video URLs. See [Media](MEDIA.md).

## Wine colours

`wineColors` supplies the red, white and rosé pages. Each bottle's `family` is one
of `Red`, `White`, `Rosé`, `Madeira Wine` or `Rum`; `style` further separates the
fortified Madeira Wine recommendations. Never assign a fortified Sercial or
Verdelho to the white table-wine family simply because the grape is white.
Atlantis product details use the Madeira Wine Company producer page. Stock and
tasting availability remain requests to confirm, not promises. Colour photographs
are illustrative and are labelled as such in their credits.
