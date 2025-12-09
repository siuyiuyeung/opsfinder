import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { deviceService } from '@/services/device.service'
import type { Device, DeviceRequest, PageResponse } from '@/types/device'

/**
 * Device store using Pinia.
 */
export const useDeviceStore = defineStore('device', () => {
  // State
  const devices = ref<Device[]>([])
  const currentDevice = ref<Device | null>(null)
  const totalElements = ref(0)
  const totalPages = ref(0)
  const currentPage = ref(0)
  const pageSize = ref(20)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Filter options
  const zones = ref<string[]>([])
  const types = ref<string[]>([])
  const datacenters = ref<string[]>([])

  // Computed
  const hasDevices = computed(() => devices.value.length > 0)
  const isLastPage = computed(() => currentPage.value >= totalPages.value - 1)
  const isFirstPage = computed(() => currentPage.value === 0)

  // Actions
  /**
   * Load filter options (zones, types, datacenters).
   */
  async function loadFilterOptions() {
    try {
      const [zonesData, typesData, datacentersData] = await Promise.all([
        deviceService.getDistinctZones(),
        deviceService.getDistinctTypes(),
        deviceService.getDistinctDatacenters(),
      ])
      zones.value = zonesData
      types.value = typesData
      datacenters.value = datacentersData
    } catch (err: any) {
      console.error('Failed to load filter options:', err)
    }
  }

  /**
   * Search devices by keyword.
   */
  async function searchDevices(
    searchTerm: string,
    page: number = 0,
    size: number = 20,
    sort: string = 'id,desc'
  ) {
    loading.value = true
    error.value = null

    try {
      const response: PageResponse<Device> = await deviceService.searchDevices(
        searchTerm,
        page,
        size,
        sort
      )
      devices.value = response.content
      totalElements.value = response.totalElements
      totalPages.value = response.totalPages
      currentPage.value = response.number
      pageSize.value = response.size
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to search devices'
      console.error('Search devices error:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * Fetch devices with optional filters.
   */
  async function fetchDevices(
    filters?: { zone?: string; type?: string },
    page: number = 0,
    size: number = 20,
    sort: string = 'id,desc'
  ) {
    loading.value = true
    error.value = null

    try {
      const response: PageResponse<Device> = await deviceService.getDevices(
        filters,
        page,
        size,
        sort
      )
      devices.value = response.content
      totalElements.value = response.totalElements
      totalPages.value = response.totalPages
      currentPage.value = response.number
      pageSize.value = response.size
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to fetch devices'
      console.error('Fetch devices error:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * Fetch device by ID.
   */
  async function fetchDeviceById(id: number) {
    loading.value = true
    error.value = null

    try {
      currentDevice.value = await deviceService.getDeviceById(id)
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to fetch device'
      console.error('Fetch device error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Create a new device.
   */
  async function createDevice(device: DeviceRequest) {
    loading.value = true
    error.value = null

    try {
      const newDevice = await deviceService.createDevice(device)
      currentDevice.value = newDevice
      return newDevice
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to create device'
      console.error('Create device error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Update an existing device.
   */
  async function updateDevice(id: number, device: DeviceRequest) {
    loading.value = true
    error.value = null

    try {
      const updatedDevice = await deviceService.updateDevice(id, device)
      currentDevice.value = updatedDevice
      // Update in list if present
      const index = devices.value.findIndex((d) => d.id === id)
      if (index !== -1) {
        devices.value[index] = updatedDevice
      }
      return updatedDevice
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to update device'
      console.error('Update device error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Delete a device.
   */
  async function deleteDevice(id: number) {
    loading.value = true
    error.value = null

    try {
      await deviceService.deleteDevice(id)
      // Remove from list
      devices.value = devices.value.filter((d) => d.id !== id)
      if (currentDevice.value?.id === id) {
        currentDevice.value = null
      }
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to delete device'
      console.error('Delete device error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Clear error message.
   */
  function clearError() {
    error.value = null
  }

  /**
   * Clear current device.
   */
  function clearCurrentDevice() {
    currentDevice.value = null
  }

  return {
    // State
    devices,
    currentDevice,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    loading,
    error,
    zones,
    types,
    datacenters,
    // Computed
    hasDevices,
    isLastPage,
    isFirstPage,
    // Actions
    loadFilterOptions,
    searchDevices,
    fetchDevices,
    fetchDeviceById,
    createDevice,
    updateDevice,
    deleteDevice,
    clearError,
    clearCurrentDevice,
  }
})
