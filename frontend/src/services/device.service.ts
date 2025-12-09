import api from './api'
import type { Device, DeviceRequest, PageResponse } from '@/types/device'

/**
 * Device service for API calls.
 */
export const deviceService = {
  /**
   * Search devices by keyword using full-text search.
   */
  async searchDevices(
    searchTerm: string,
    page: number = 0,
    size: number = 20,
    sort: string = 'id,desc'
  ): Promise<PageResponse<Device>> {
    const response = await api.get<PageResponse<Device>>('/devices/search', {
      params: { q: searchTerm, page, size, sort }
    })
    return response.data
  },

  /**
   * Get all devices with optional filters.
   */
  async getDevices(
    filters?: { zone?: string; type?: string },
    page: number = 0,
    size: number = 20,
    sort: string = 'id,desc'
  ): Promise<PageResponse<Device>> {
    const response = await api.get<PageResponse<Device>>('/devices', {
      params: { ...filters, page, size, sort }
    })
    return response.data
  },

  /**
   * Get device by ID.
   */
  async getDeviceById(id: number): Promise<Device> {
    const response = await api.get<Device>(`/devices/${id}`)
    return response.data
  },

  /**
   * Get distinct zones for filtering.
   */
  async getDistinctZones(): Promise<string[]> {
    const response = await api.get<string[]>('/devices/filters/zones')
    return response.data
  },

  /**
   * Get distinct types for filtering.
   */
  async getDistinctTypes(): Promise<string[]> {
    const response = await api.get<string[]>('/devices/filters/types')
    return response.data
  },

  /**
   * Get distinct datacenters for filtering.
   */
  async getDistinctDatacenters(): Promise<string[]> {
    const response = await api.get<string[]>('/devices/filters/datacenters')
    return response.data
  },

  /**
   * Create a new device.
   */
  async createDevice(device: DeviceRequest): Promise<Device> {
    const response = await api.post<Device>('/devices', device)
    return response.data
  },

  /**
   * Update an existing device.
   */
  async updateDevice(id: number, device: DeviceRequest): Promise<Device> {
    const response = await api.put<Device>(`/devices/${id}`, device)
    return response.data
  },

  /**
   * Delete a device.
   */
  async deleteDevice(id: number): Promise<void> {
    await api.delete(`/devices/${id}`)
  },

  /**
   * Get device count by zone.
   */
  async countDevicesByZone(zone: string): Promise<number> {
    const response = await api.get<number>(`/devices/stats/zone/${zone}`)
    return response.data
  },

  /**
   * Get device count by type.
   */
  async countDevicesByType(type: string): Promise<number> {
    const response = await api.get<number>(`/devices/stats/type/${type}`)
    return response.data
  },
}
