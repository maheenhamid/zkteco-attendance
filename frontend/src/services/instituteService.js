import instituteApiClient, { normalizeInstituteApiError } from './instituteApiClient';

// Maps the raw https://api.shebashikkha.com response onto the clean
// { id, name, ... } shape every consumer (useInstitutes hook, Users.jsx,
// Devices.jsx, etc.) already expects, so nothing downstream needs to change.
// Upstream's institute id field is literally spelled "instiltuteId" (typo,
// confirmed against the live API) - both spellings are accepted defensively
// in case it's ever corrected server-side.
function mapInstitute(raw) {
  return {
    id: raw.id ?? raw.instiltuteId ?? raw.instituteId ?? null,
    name: raw.name ?? raw.instituteName ?? '',
    address: raw.address ?? '',
  };
}

export async function fetchInstitutes() {
  try {
    const { data } = await instituteApiClient.get('/institute/list/for/zkteco');
    return (data?.item ?? []).map(mapInstitute);
  } catch (error) {
    throw normalizeInstituteApiError(error, 'Fetching institute list');
  }
}
