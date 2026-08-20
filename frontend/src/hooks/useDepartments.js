import { useQuery } from '@tanstack/react-query';
import { fetchDepartments } from '../services/departmentService';

export function useDepartments(instituteId) {
  return useQuery(['departments', instituteId], () => fetchDepartments(instituteId), {
    enabled: !!instituteId,
    staleTime: 5 * 60 * 1000,
  });
}
