import { useQuery } from '@tanstack/react-query';
import { fetchClasses } from '../services/instituteService';

export function useClasses(instituteId) {
  return useQuery(['classes', instituteId], () => fetchClasses(instituteId), {
    enabled: !!instituteId,
    staleTime: 5 * 60 * 1000,
  });
}
